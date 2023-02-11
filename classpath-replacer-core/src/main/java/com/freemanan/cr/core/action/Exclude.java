package com.freemanan.cr.core.action;

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

    public static Exclude of(String... patterns) {
        Exclude exclude = new Exclude();
        exclude.patterns.addAll(List.of(patterns));
        return exclude;
    }
}
