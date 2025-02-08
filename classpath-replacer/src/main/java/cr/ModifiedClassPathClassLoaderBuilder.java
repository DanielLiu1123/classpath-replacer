package cr;

import cr.action.Add;
import cr.action.Exclude;
import cr.util.Const;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Freeman
 */
public class ModifiedClassPathClassLoaderBuilder {
    private static final Pattern INTELLIJ_CLASSPATH_JAR_PATTERN = Pattern.compile(".*classpath(\\d+)?\\.jar");

    private final List<Add> adds = new LinkedList<>();
    private final List<Exclude> excludes = new LinkedList<>();
    private final ClassLoader parent;

    /**
     * Nullable, because {@link ModifiedClassPathClassLoaderBuilder} may be used to programmatically generate classloader.
     */
    private Classpath classpath;

    ModifiedClassPathClassLoaderBuilder(ClassLoader parent) {
        this.parent = parent;
    }

    public ModifiedClassPathClassLoaderBuilder exclude(String... patterns) {
        excludes.add(Exclude.of(patterns));
        return this;
    }

    public ModifiedClassPathClassLoaderBuilder add(String... coordinates) {
        adds.add(Add.of(coordinates));
        return this;
    }

    public ModifiedClassPathClassLoaderBuilder classpathReplacer(Classpath classpath) {
        this.classpath = classpath;
        return this;
    }

    public ModifiedClassPathClassLoader build() {
        URL[] urls = extractUrls(parent);
        List<URL> result = Arrays.stream(urls)
                .collect(Collectors.toCollection(
                        LinkedList::new)); // we may have some exclude actions, LinkedList is better
        // add first, then exclude
        for (Add add : adds) {
            add(result, add);
        }
        for (Exclude exclude : excludes) {
            exclude(result, exclude);
        }
        return new ModifiedClassPathClassLoader(result.toArray(new URL[0]), parent.getParent(), parent);
    }

    private void add(List<URL> result, Add add) {
        // Add to the beginning of the list to make sure the added jars are loaded first.
        for (String coordinate : add.coordinates()) {
            result.addAll(0, MavenUtils.resolveCoordinate(coordinate));
        }
    }

    private void exclude(List<URL> result, Exclude exclude) {
        // com.google.code.gson:gson:2.8.6 -> [file:~/.m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar]
        Map<String, List<URL>> patternToJars = new HashMap<>();
        // com.google.code.gson:gson -> [2.8.6, 2.8.7]
        Map<String, List<String>> patternToVersions = new HashMap<>();

        List<URL> copy = new ArrayList<>(result);
        for (String pattern : exclude.patterns()) {
            Supplier<Map<String, List<String>>> patternToVersionsSupplier = () -> {
                patternToVersions.computeIfAbsent(pattern, k -> findVersions(copy, pattern));
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
        boolean excludeTransitive = classpath != null && classpath.excludeTransitive();

        if (pattern.matches(Const.MAVEN_COORDINATE_WITH_VERSION_PATTERN)) {
            return matchMavenCoordinateWithVersionPattern(excludeTransitive, url, pattern, patternToJars);
        }

        if (pattern.matches(Const.MAVEN_COORDINATE_PATTERN)) {
            return matchMavenCoordinatePattern(
                    excludeTransitive, url, pattern, patternToJars, patternToVersionsSupplier);
        }

        if (pattern.contains("*")) {
            return matchWildcardPattern(pattern, url);
        }

        if (pattern.endsWith(".jar")) {
            return matchExactFileNamePattern(pattern, url);
        }

        throw new IllegalArgumentException(String.format(Const.EXCLUDE_ILLEGAL_PATTERN_MESSAGE_FORMAT, pattern));
    }

    private boolean matchMavenCoordinateWithVersionPattern(
            boolean recursiveExclude, URL url, String pattern, Map<String, List<URL>> patternToJars) {
        if (!recursiveExclude) {
            String[] gav = pattern.split(":");
            if (!isSameGroupIdWithExactMatch(url, gav[0].split("\\."))) {
                return false;
            }
            String artifactId = gav[1];
            String version = gav[2];
            String jarName = String.format("%s-%s.jar", artifactId, version);
            return Objects.equals(jarName, fileName(url));
        }

        return patternToJars.computeIfAbsent(pattern, s -> MavenUtils.resolveCoordinate(pattern)).stream()
                .anyMatch(jarPath -> isSameJar(url, jarPath));
    }

    private boolean matchMavenCoordinatePattern(
            boolean recursiveExclude,
            URL url,
            String pattern,
            Map<String, List<URL>> patternToJars,
            Supplier<Map<String, List<String>>> patternToVersionsSupplier) {
        if (!recursiveExclude) {
            String[] gav = pattern.split(":");
            if (!isSameGroupIdWithExactMatch(url, gav[0].split("\\."))) {
                return false;
            }
            String artifactId = gav[1];
            String fileName = fileName(url);
            if (fileName == null || !fileName.startsWith(artifactId)) {
                return false;
            }
            String version =
                    fileName.substring(artifactId.length() + "-".length(), fileName.length() - ".jar".length());
            return version.matches(Const.VERSION_PATTERN);
        }

        return patternToVersionsSupplier.get().getOrDefault(pattern, Collections.emptyList()).stream()
                .anyMatch(version -> {
                    String coordinate = pattern + ":" + version;
                    return patternToJars
                            .computeIfAbsent(coordinate, s -> MavenUtils.resolveCoordinate(coordinate))
                            .stream()
                            .anyMatch(jarPath -> isSameJar(url, jarPath));
                });
    }

    private boolean matchWildcardPattern(String pattern, URL url) {
        String regex = pattern.replace(".", "\\.").replace("*", ".*");
        return Pattern.matches(regex, fileName(url));
    }

    private boolean matchExactFileNamePattern(String pattern, URL url) {
        return pattern.equals(fileName(url));
    }

    private static boolean isSameJar(URL url, URL mavenJarUrl) {
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
        String mavenJarStr = mavenJarUrl.toString();
        String temp = mavenJarStr.substring(0, mavenJarStr.lastIndexOf("/"));
        temp = temp.substring(0, temp.lastIndexOf("/"));
        String[] arr = temp.substring(0, temp.lastIndexOf("/")).split("/");
        return isSameGroupIdWithFuzzyMatch(url, arr);
    }

    /**
     * Only take the last 2 elements of the group id array to compare.
     *
     * @param url        jar url
     * @param groupIdArr maven group id array, e.g. [com, google, code, gson]
     * @return true if the group id is same
     */
    private static boolean isSameGroupIdWithFuzzyMatch(URL url, String[] groupIdArr) {
        String urlStr = url.toString();

        // gradle project, jar cache path is like
        // ~/.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson/2.8.6/3f7e1e9e8e1b0e1e8e1b0e1b0e1b0e1b0e1b0e1b/gson-2.8.6.jar
        String gradlePartGroupId = groupIdArr[groupIdArr.length - 2] + "." + groupIdArr[groupIdArr.length - 1] + "/";
        if (urlStr.contains(gradlePartGroupId)) {
            return true;
        }

        // maven project, jar cache path is like ~/.m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar
        String mavenPartGroupId =
                "/" + groupIdArr[groupIdArr.length - 2] + "/" + groupIdArr[groupIdArr.length - 1] + "/";
        if (urlStr.contains(mavenPartGroupId)) {
            return true;
        }

        // TODO: here to support the other project types, non maven and non gradle

        // artifact id is the same, but group id is different
        return false;
    }

    /**
     * Url must contain all the elements in groupIdArr.
     *
     * @param url        jar url
     * @param groupIdArr maven group id array, e.g. [com, google, code, gson]
     * @return true if the group id is same
     */
    private static boolean isSameGroupIdWithExactMatch(URL url, String[] groupIdArr) {
        String urlStr = url.toString();

        // gradle project, jar cache path is like
        // ~/.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson/2.8.6/3f7e1e9e8e1b0e1e8e1b0e1b0e1b0e1b0e1b0e1b/gson-2.8.6.jar
        String gradlePartGroupId = "/" + String.join(".", groupIdArr) + "/";
        if (urlStr.contains(gradlePartGroupId)) {
            return true;
        }

        // maven project, jar cache path is like ~/.m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar
        String mavenPartGroupId = "/" + String.join("/", groupIdArr) + "/";
        if (urlStr.contains(mavenPartGroupId)) {
            return true;
        }

        // TODO: here to support the other project types, non maven and non gradle

        // artifact id is the same, but group id is different
        return false;
    }

    private static List<String> findVersions(List<URL> urls, String groupAndArtifact) {
        List<String> versions = new ArrayList<>();
        String[] ga = groupAndArtifact.split(":");
        String artifactId = ga[1];
        String regex = String.format("%s-(.*)\\.jar", artifactId);
        Pattern pattern = Pattern.compile(regex);
        for (URL url : urls) {
            String fileName = fileName(url);
            if (fileName == null) {
                continue;
            }
            if (!isSameGroupIdWithExactMatch(url, ga[0].split("\\."))) {
                continue;
            }
            Matcher matcher = pattern.matcher(fileName);
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
                .map(ModifiedClassPathClassLoaderBuilder::toURL);
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
}
