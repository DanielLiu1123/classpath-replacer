package com.freemanan.cr.core.action;

import static com.freemanan.cr.core.util.Const.JAR_FILE_NAME_PATTERN;
import static com.freemanan.cr.core.util.Const.MAVEN_COORDINATE_PATTERN;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Freeman
 */
public class Exclude {

    /**
     * File name patterns.
     *
     * <p> e.g. gson-*.jar, *-starter-*.jar
     */
    private final List<String> patterns = new ArrayList<>();

    private Exclude() {}

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
        List<String> fileNamePatterns = new ArrayList<>();
        for (String pattern : patterns) {
            if (pattern.matches(JAR_FILE_NAME_PATTERN)) {
                fileNamePatterns.add(pattern);
            } else if (pattern.matches(MAVEN_COORDINATE_PATTERN)) {
                String[] gav = pattern.split(":");
                boolean hasVersion = gav.length == 3;
                String artifactId = gav[1];
                String filename = artifactId + "-" + (hasVersion ? gav[2] : "*") + ".jar";
                fileNamePatterns.add(filename);
            } else {
                throw new IllegalArgumentException("Invalid pattern: " + pattern);
            }
        }
        Exclude exclude = new Exclude();
        exclude.patterns.addAll(List.copyOf(fileNamePatterns));
        return exclude;
    }
}
