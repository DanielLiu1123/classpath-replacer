package com.freemanan.cr.core.action;

import static com.freemanan.cr.core.util.Const.JAR_FILE_NAME_PATTERN;
import static com.freemanan.cr.core.util.Const.MAVEN_COORDINATE_PATTERN;
import static com.freemanan.cr.core.util.Const.MAVEN_COORDINATE_WITH_VERSION_PATTERN;

import com.freemanan.cr.core.ModifiedClassPathClassLoaderGenerator;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Freeman
 */
public class Exclude {

    /**
     * File name patterns.
     *
     * <p> Two patterns:
     * <p> 1. Jar file name pattern, like {@code gson-*.jar}
     * <p> 2. Maven coordinate pattern, like {@code com.google.code.gson:gson:2.8.6} or {@code com.google.code.gson:gson} (without version will exclude all versions)
     */
    private final List<String> patterns = new ArrayList<>();

    private Exclude() {}

    /**
     * File name patterns.
     *
     * <p> Two patterns:
     * <p> 1. Jar file name pattern, like {@code gson-*.jar}
     * <p> 2. Maven coordinate pattern, like {@code com.google.code.gson:gson:2.8.6} or {@code com.google.code.gson:gson} (without version will exclude all versions)
     */
    public List<String> patterns() {
        return List.copyOf(patterns);
    }

    /**
     * Build {@link Exclude} from patterns.
     *
     * <p> support two patterns:
     * <p> 1. Jar file name pattern: gson-*.jar
     * <p> 2. Maven coordinate pattern: com.google.code.gson:gson:2.8.6, com.google.code.gson:gson (without version will exclude all versions)
     *
     * @param patterns patterns
     * @return {@link Exclude}
     */
    public static Exclude of(String... patterns) {
        List<String> patternList = new ArrayList<>();
        for (String pattern : patterns) {
            if (pattern.matches(JAR_FILE_NAME_PATTERN) || pattern.matches(MAVEN_COORDINATE_PATTERN)) {
                patternList.add(pattern);
            } else {
                throw new IllegalArgumentException("Invalid pattern: " + pattern);
            }
        }
        Exclude exclude = new Exclude();
        exclude.patterns.addAll(List.copyOf(patternList));
        return exclude;
    }

    /**
     * {"com.google.code.gson:gson:2.8.6": [file:~/.m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar]}
     */
    public Map<String, List<URL>> coordinateMap() {
        Map<String, List<URL>> result = new HashMap<>();
        for (String pattern : patterns) {
            if (!pattern.matches(MAVEN_COORDINATE_WITH_VERSION_PATTERN)) {
                continue;
            }
            List<URL> urls = ModifiedClassPathClassLoaderGenerator.resolveCoordinates(new String[] {pattern});
            result.put(pattern, urls);
        }
        return result;
    }
}
