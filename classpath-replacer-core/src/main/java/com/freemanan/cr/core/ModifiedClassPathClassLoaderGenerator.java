package com.freemanan.cr.core;

import com.freemanan.cr.core.action.Add;
import com.freemanan.cr.core.action.Exclude;
import com.freemanan.cr.core.action.Override;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
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

/**
 * @author Freeman
 */
public class ModifiedClassPathClassLoaderGenerator {
    private final List<Object> actions = new LinkedList<>();
    private boolean recursiveExclude = false;

    private static final Pattern INTELLIJ_CLASSPATH_JAR_PATTERN = Pattern.compile(".*classpath(\\d+)?\\.jar");
    private static final int MAX_RESOLUTION_ATTEMPTS = 3;

    private final ClassLoader parent;

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

    public ModifiedClassPathClassLoaderGenerator recursiveExclude(boolean recursiveExclude) {
        this.recursiveExclude = recursiveExclude;
        return this;
    }

    public ModifiedClassPathClassLoader gen() {
        URL[] urls = extractUrls(parent);
        List<URL> result = Arrays.stream(urls).collect(Collectors.toList());
        actions.forEach(action -> {
            if (action instanceof Exclude exclude) {
                exclude(result, exclude, recursiveExclude);
            } else if (action instanceof Add add) {
                add(result, add);
            } else if (action instanceof Override override) {
                override(result, override);
            }
        });
        return new ModifiedClassPathClassLoader(result.toArray(URL[]::new), parent.getParent(), parent);
    }

    private static void override(List<URL> result, Override override) {
        // have same behavior as add
        add(result, Add.of(override.coordinates().toArray(String[]::new)));
    }

    private static void add(List<URL> result, Add add) {
        List<String> coordinates =
                add.coordinates().stream().sorted(Comparator.reverseOrder()).toList();
        // Add to the beginning of the list to make sure the added jars are loaded first.
        result.addAll(0, getAdditionalUrls(coordinates));
    }

    private static void exclude(List<URL> result, Exclude exclude, boolean recursiveExclude) {
        Map<String, List<URL>> patternToJars = new HashMap<>();
        Set<String> removed = new HashSet<>();
        result.removeIf(
                url -> exclude.patterns().stream().anyMatch(pattern -> {
                    if (pattern.matches(Const.MAVEN_COORDINATE_WITH_VERSION_PATTERN)) {
                        if (recursiveExclude) {
                            // if the pattern is a maven coordinate, parse out all the jar packages it depends on
                            // like com.google.code.gson:gson:2.8.6
                            // we only remove once!
                            if (removed.add(url.toString())) {
                                // still not removed
                                List<String> urls = patternToJars
                                        .computeIfAbsent(pattern, s -> resolveCoordinates(new String[] {pattern}))
                                        .stream()
                                        .map(Objects::toString)
                                        .toList();
                                return urls.contains(url.toString());
                            }
                            // already removed
                            return false;
                        }
                        String[] gav = pattern.split(":");
                        String artifactId = gav[1];
                        String version = gav[2];
                        String jarName = artifactId + "-" + version + ".jar";
                        return jarName.equals(fileName(url));
                    }
                    // like gson-*.jar
                    if (pattern.contains("*")) {
                        String regex = pattern.replace(".", "\\.").replace("*", ".*");
                        return Pattern.matches(regex, fileName(url));
                    }
                    // like gson-2.8.6.jar
                    return pattern.equals(fileName(url));
                }));
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
        if (classLoader instanceof URLClassLoader urlClassLoader) {
            return Stream.of(urlClassLoader.getURLs());
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

    private static List<URL> getAdditionalUrls(List<String> coordinates) {
        if (coordinates.isEmpty()) {
            return Collections.emptyList();
        }
        return resolveCoordinates(coordinates.toArray(new String[0]));
    }

    public static List<URL> resolveCoordinates(String[] coordinates) {
        DefaultServiceLocator serviceLocator = MavenRepositorySystemUtils.newServiceLocator();
        serviceLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        serviceLocator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        RepositorySystem repositorySystem = serviceLocator.getService(RepositorySystem.class);

        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepository = new LocalRepository(System.getProperty("user.home") + "/.m2/repository");
        RemoteRepository remoteRepository =
                new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2").build();
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepository));
        Exception latestFailure = null;
        for (int i = 0; i < MAX_RESOLUTION_ATTEMPTS; i++) {
            CollectRequest collectRequest = new CollectRequest(null, List.of(remoteRepository));
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

    private static List<Dependency> createDependencies(String[] allCoordinates) {
        List<Dependency> dependencies = new ArrayList<>();
        for (String coordinate : allCoordinates) {
            dependencies.add(new Dependency(new DefaultArtifact(coordinate), null));
        }
        return dependencies;
    }
}
