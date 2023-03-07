package com.freemanan.cr.core.action;

import static com.freemanan.cr.core.util.Const.MAVEN_COORDINATE_WITH_VERSION_PATTERN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Freeman
 */
public class Add {

    private final List<String> coordinates = new ArrayList<>();

    private Add() {}

    public List<String> coordinates() {
        return Collections.unmodifiableList(coordinates);
    }

    public static Add of(String... coordinates) {
        List<String> validCoordinates = new ArrayList<>();
        for (String coordinate : coordinates) {
            if (coordinate.matches(MAVEN_COORDINATE_WITH_VERSION_PATTERN)) {
                validCoordinates.add(coordinate);
            } else {
                throw new IllegalArgumentException("Invalid maven coordinate: " + coordinate);
            }
        }
        Add add = new Add();
        add.coordinates.addAll(validCoordinates);
        return add;
    }
}
