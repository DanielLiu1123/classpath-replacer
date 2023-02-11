package com.freemanan.cr.core.action;

import static com.freemanan.cr.core.util.Const.*;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Freeman
 */
public class Add {
    private static final Logger log = LoggerFactory.getLogger(Add.class);

    private final List<String> coordinates = new ArrayList<>();

    private Add() {}

    public List<String> coordinates() {
        return List.copyOf(coordinates);
    }

    public static Add of(String... coordinates) {
        List<String> validCoordinates = new ArrayList<>();
        for (String coordinate : coordinates) {
            if (coordinate.matches(MAVEN_COORDINATE_WITH_VERSION_PATTERN)) {
                validCoordinates.add(coordinate);
            } else {
                log.warn("Invalid maven coordinate: {}", coordinate);
            }
        }
        Add add = new Add();
        add.coordinates.addAll(List.copyOf(validCoordinates));
        return add;
    }
}
