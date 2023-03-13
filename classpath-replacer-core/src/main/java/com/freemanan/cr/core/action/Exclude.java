package com.freemanan.cr.core.action;

import static com.freemanan.cr.core.util.Const.JAR_FILE_NAME_PATTERN;
import static com.freemanan.cr.core.util.Const.MAVEN_COORDINATE_PATTERN;

import com.freemanan.cr.core.util.Const;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        return Collections.unmodifiableList(patterns);
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
            if (!pattern.matches(JAR_FILE_NAME_PATTERN) && !pattern.matches(MAVEN_COORDINATE_PATTERN)) {
                throw new IllegalArgumentException(
                        String.format(Const.EXCLUDE_ILLEGAL_PATTERN_MESSAGE_FORMAT, pattern));
            }
            patternList.add(pattern);
        }
        Exclude exclude = new Exclude();
        exclude.patterns.addAll(patternList);
        return exclude;
    }
}
