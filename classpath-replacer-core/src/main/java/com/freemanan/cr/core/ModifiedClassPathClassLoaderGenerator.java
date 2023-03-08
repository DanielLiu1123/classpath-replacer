package com.freemanan.cr.core;

import com.freemanan.cr.core.action.Add;
import com.freemanan.cr.core.action.Exclude;
import com.freemanan.cr.core.action.Override;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import com.freemanan.cr.core.anno.Repository;
import com.freemanan.cr.core.util.Const;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

/**
 * @author Freeman
 */
public class ModifiedClassPathClassLoaderGenerator {
    private static final Pattern INTELLIJ_CLASSPATH_JAR_PATTERN = Pattern.compile(".*classpath(\\d+)?\\.jar");
    private static final int MAX_RESOLUTION_ATTEMPTS = 3;

    private final List<Object> actions = new LinkedList<>();
    private final ClassLoader parent;

    /**
     * Nullable, because {@link ModifiedClassPathClassLoaderGenerator} may be used to programmatically generate classloader.
     */
    private ClasspathReplacer classpathReplacer;

    private ModifiedClassPathClassLoaderGenerator(ClassLoader parent) {
        this.parent = parent;
    }

    public static ModifiedClassPathClassLoaderGenerator of(ClassLoader parent) {
        return new ModifiedClassPathClassLoaderGenerator(parent);
    }

    public ModifiedClassPathClassLoaderGenerator exclude(String... patterns) {
        actions.add(Exclude.of(patterns));
        return this;
    }

    public ModifiedClassPathClassLoaderGenerator add(String... coordinates) {
        actions.add(Add.of(coordinates));
        return this;
    }

    public ModifiedClassPathClassLoaderGenerator override(String... coordinates) {
        actions.add(Override.of(coordinates));
        return this;
    }

    public ModifiedClassPathClassLoaderGenerator classpathReplacer(ClasspathReplacer classpathReplacer) {
        this.classpathReplacer = classpathReplacer;
        return this;
    }

    public ModifiedClassPathClassLoader gen() {
        URL[] urls = extractUrls(parent);
        List<URL> result = Arrays.stream(urls)
                .collect(Collectors.toCollection(
                        LinkedList::new)); // we may have some exclude actions, LinkedList is better
        actions.forEach(action -> {
            if (action instanceof Exclude) {
                exclude(result, (Exclude) action);
            } else if (action instanceof Add) {
                add(result, (Add) action);
            } else if (action instanceof Override) {
                override(result, (Override) action);
            } else {
                throw new IllegalArgumentException("Unknown action: " + action);
            }
        });
        return new ModifiedClassPathClassLoader(result.toArray(new URL[0]), parent.getParent(), parent);
    }

    private void override(List<URL> result, Override override) {
        // have same behavior as add
        add(result, Add.of(override.coordinates().toArray(new String[0])));
    }

    private void add(List<URL> result, Add add) {
        List<String> coordinates =
                add.coordinates().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        // Add to the beginning of the list to make sure the added jars are loaded first.
        result.addAll(0, getAdditionalUrls(coordinates));
    }

    private void exclude(List<URL> result, Exclude exclude) {
        // com.google.code.gson:gson:2.8.6 -> [file:~/.m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar]
        Map<String, List<URL>> patternToJars = new HashMap<>();
        // com.google.code.gson:gson -> [2.8.6, 2.8.7]
        Map<String, List<String>> patternToVersions = new HashMap<>();

        List<URL> copy = new ArrayList<>(result);
        for (String pattern : exclude.patterns()) {
            Supplier<Map<String, List<String>>> patternToVersionsSupplier = () -> {
                patternToVersions.putIfAbsent(pattern, findVersions(copy, pattern));
                return patternToVersions;
            };
            for (URL url : copy) {
                if (needRemove(patternToVersionsSupplier, url, pattern, patternToJars)) {
                    result.remove(url);
                }
            }
        }
    }

    private boolean needRemove(
            Supplier<Map<String, List<String>>> patternToVersionsSupplier,
            URL url,
            String pattern,
            Map<String, List<URL>> patternToJars) {
        boolean recursiveExclude = Optional.ofNullable(classpathReplacer)
                .map(ClasspathReplacer::recursiveExclude)
                .orElse(false);
        // like com.google.code.gson:gson:2.8.6
        if (pattern.matches(Const.MAVEN_COORDINATE_WITH_VERSION_PATTERN)) {
            if (!recursiveExclude) {
                String[] gav = pattern.split(":");
                String artifactId = gav[1];
                String version = gav[2];
                String jarName = artifactId + "-" + version + ".jar";
                return jarName.equals(fileName(url));
            }
            return patternToJars
                    .computeIfAbsent(pattern, s -> resolveCoordinates(new String[] {pattern}, classpathReplacer))
                    .stream()
                    .anyMatch(jarPath -> isSameJar(url, jarPath));
        }

        // like com.google.code.gson:gson
        if (pattern.matches(Const.MAVEN_COORDINATE_PATTERN)) {
            if (!recursiveExclude) {
                String[] gav = pattern.split(":");
                String artifactId = gav[1];

                // like gson-2.8.6.jar
                String fileName = fileName(url);
                if (fileName == null || !fileName.startsWith(artifactId)) {
                    return false;
                }

                // extract version
                String version = fileName.substring(artifactId.length() + 1, fileName.length() - 4);
                // it may be api-1.0.0
                if (!version.matches(Const.VERSION_PATTERN)) {
                    return false;
                }

                String file = String.format("%s-%s.jar", artifactId, version);
                return file.equals(fileName);
            }
            return patternToVersionsSupplier.get().getOrDefault(pattern, Collections.emptyList()).stream()
                    .anyMatch(version -> {
                        String coordinate = pattern + ":" + version;
                        return patternToJars
                                .computeIfAbsent(
                                        coordinate,
                                        s -> resolveCoordinates(new String[] {coordinate}, classpathReplacer))
                                .stream()
                                .anyMatch(jarPath -> isSameJar(url, jarPath));
                    });
        }

        // like gson-*.jar
        if (pattern.contains("*")) {
            String regex = pattern.replace(".", "\\.").replace("*", ".*");
            return Pattern.matches(regex, fileName(url));
        }

        // like gson-2.8.6.jar
        if (pattern.endsWith(".jar")) {
            return pattern.equals(fileName(url));
        }

        // illegal pattern
        throw new IllegalArgumentException("Illegal pattern: " + pattern);
    }

    private static boolean isSameJar(URL url, URL mavenJarUrl) {
        // if gradle project, jar cache path is like
        // ~/.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson/2.8.6/3f7e1e9e8e1b0e1e8e1b0e1b0e1b0e1b0e1b0e1b/gson-2.8.6.jar
        // if maven project, jar cache path is like ~/.m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar
        if (url == null || mavenJarUrl == null) {
            return false;
        }

        if (Objects.equals(url, mavenJarUrl)) {
            return true;
        }

        // compare file name
        String fileName = fileName(url);
        if (fileName == null) {
            return false;
        }
        String jarFileName = fileName(mavenJarUrl);
        if (!Objects.equals(fileName, jarFileName)) {
            return false;
        }

        // compare group id
        // remove the last 3 '/' for maven jar path
        String urlStr = url.toString();
        String mavenJarStr = mavenJarUrl.toString();
        String temp = mavenJarStr.substring(0, mavenJarStr.lastIndexOf("/"));
        temp = temp.substring(0, temp.lastIndexOf("/"));
        String[] arr = temp.substring(0, temp.lastIndexOf("/")).split("/");

        // gradle
        String gradlePartGroupId = arr[arr.length - 2] + "." + arr[arr.length - 1];
        if (urlStr.contains(gradlePartGroupId)) {
            return true;
        }

        // maven
        String mavenPartGroupId = arr[arr.length - 2] + "/" + arr[arr.length - 1];
        if (urlStr.contains(mavenPartGroupId)) {
            return true;
        }

        // TODO: here to support the other project types, non maven and non gradle

        // artifact id is the same, but group id is different
        return false;
    }

    private static List<String> findVersions(List<URL> urls, String groupAndArtifact) {
        List<String> versions = new ArrayList<>();
        for (URL url : urls) {
            String fileName = fileName(url);
            if (fileName == null) {
                continue;
            }
            String[] ga = groupAndArtifact.split(":");
            String artifactId = ga[1];
            String regex = String.format("%s-(.*)\\.jar", artifactId);
            Matcher matcher = Pattern.compile(regex).matcher(fileName);
            if (matcher.find()) {
                String version = matcher.group(1);
                if (version.matches(Const.VERSION_PATTERN)) {
                    versions.add(version);
                }
            }
        }
        return versions;
    }

    private static String fileName(URL url) {
        if (!"file".equals(url.getProtocol())) {
            return null;
        }
        try {
            return new File(url.toURI()).getName();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static URL[] extractUrls(ClassLoader classLoader) {
        List<URL> extractedUrls = new ArrayList<>();
        doExtractUrls(classLoader).forEach((URL url) -> {
            if (isManifestOnlyJar(url)) {
                extractedUrls.addAll(extractUrlsFromManifestClassPath(url));
            } else {
                extractedUrls.add(url);
            }
        });
        return extractedUrls.toArray(new URL[0]);
    }

    private static Stream<URL> doExtractUrls(ClassLoader classLoader) {
        if (classLoader instanceof URLClassLoader) {
            return Stream.of(((URLClassLoader) classLoader).getURLs());
        }
        return Stream.of(ManagementFactory.getRuntimeMXBean().getClassPath().split(File.pathSeparator))
                .map(ModifiedClassPathClassLoaderGenerator::toURL);
    }

    private static URL toURL(String entry) {
        try {
            return new File(entry).toURI().toURL();
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private static boolean isManifestOnlyJar(URL url) {
        return isShortenedIntelliJJar(url);
    }

    private static boolean isShortenedIntelliJJar(URL url) {
        String urlPath = url.getPath();
        boolean isCandidate = INTELLIJ_CLASSPATH_JAR_PATTERN.matcher(urlPath).matches();
        if (isCandidate) {
            try {
                Attributes attributes = getManifestMainAttributesFromUrl(url);
                String createdBy = attributes.getValue("Created-By");
                return createdBy != null && createdBy.contains("IntelliJ");
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private static List<URL> extractUrlsFromManifestClassPath(URL bootJar) {
        List<URL> urls = new ArrayList<>();
        try {
            for (String entry : getClassPath(bootJar)) {
                urls.add(new URL(entry));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return urls;
    }

    private static String[] getClassPath(URL bootJar) throws Exception {
        Attributes attributes = getManifestMainAttributesFromUrl(bootJar);
        String classpathListStr = attributes.getValue(Attributes.Name.CLASS_PATH);
        return classpathListStr.split(" ");
    }

    private static Attributes getManifestMainAttributesFromUrl(URL url) throws Exception {
        try (JarFile jarFile = new JarFile(new File(url.toURI()))) {
            return jarFile.getManifest().getMainAttributes();
        }
    }

    private List<URL> getAdditionalUrls(List<String> coordinates) {
        if (coordinates.isEmpty()) {
            return Collections.emptyList();
        }
        return resolveCoordinates(coordinates.toArray(new String[0]), classpathReplacer);
    }

    /**
     * Resolves Maven coordinates to a list of URLs.
     *
     * @param coordinates       Maven coordinates of the form groupId:artifactId:version
     * @param classpathReplacer classpath replacer, nullable
     * @return list of URLs to the resolved artifacts
     */
    public static List<URL> resolveCoordinates(String[] coordinates, ClasspathReplacer classpathReplacer) {
        DefaultServiceLocator serviceLocator = MavenRepositorySystemUtils.newServiceLocator();
        serviceLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        serviceLocator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        RepositorySystem repositorySystem = serviceLocator.getService(RepositorySystem.class);

        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepository = new LocalRepository(System.getProperty("user.home") + "/.m2/repository");
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepository));
        Exception latestFailure = null;
        for (int i = 0; i < MAX_RESOLUTION_ATTEMPTS; i++) {
            CollectRequest collectRequest = new CollectRequest(null, allRepositories(classpathReplacer));
            collectRequest.setDependencies(createDependencies(coordinates));
            DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
            try {
                DependencyResult result = repositorySystem.resolveDependencies(session, dependencyRequest);
                List<URL> resolvedArtifacts = new ArrayList<>();
                for (ArtifactResult artifact : result.getArtifactResults()) {
                    resolvedArtifacts.add(
                            artifact.getArtifact().getFile().toURI().toURL());
                }
                return resolvedArtifacts;
            } catch (Exception ex) {
                latestFailure = ex;
            }
        }
        throw new IllegalStateException(
                "Resolution failed after " + MAX_RESOLUTION_ATTEMPTS + " attempts", latestFailure);
    }

    private static List<RemoteRepository> allRepositories(ClasspathReplacer classpathReplacer) {
        List<RemoteRepository> extra = extraRepositories(classpathReplacer);
        RemoteRepository central = centralRepository();
        List<RemoteRepository> result = new ArrayList<>(extra);
        result.add(central);
        return result;
    }

    private static List<RemoteRepository> extraRepositories(ClasspathReplacer classpathReplacer) {
        if (classpathReplacer == null) {
            return Collections.emptyList();
        }
        List<RemoteRepository> extraRepositories = new ArrayList<>(classpathReplacer.repositories().length);
        for (Repository repo : classpathReplacer.repositories()) {
            String id = (repo.id() == null || repo.id().isEmpty()) ? repo.value() : repo.id();
            String url = repo.value();
            RemoteRepository.Builder builder = new RemoteRepository.Builder(id, "default", url);
            String username = parseIfNecessary(repo.username());
            String password = parseIfNecessary(repo.password());
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                builder.setAuthentication(new AuthenticationBuilder()
                        .addUsername(username)
                        .addPassword(password)
                        .build());
            }
            RemoteRepository repository = builder.build();
            extraRepositories.add(repository);
        }
        return extraRepositories;
    }

    private static String parseIfNecessary(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.startsWith("${") && str.endsWith("}")) {
            String variable = str.substring(2, str.length() - 1);
            String v = Optional.ofNullable(System.getenv(variable)).orElse(System.getProperty(variable));
            return v != null ? v : str;
        }
        return str;
    }

    private static RemoteRepository centralRepository() {
        return new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2").build();
    }

    private static List<Dependency> createDependencies(String[] coordinates) {
        List<Dependency> dependencies = new ArrayList<>();
        for (String coordinate : coordinates) {
            dependencies.add(new Dependency(new DefaultArtifact(coordinate), null));
        }
        return dependencies;
    }
}
