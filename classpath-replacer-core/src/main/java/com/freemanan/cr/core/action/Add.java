package com.freemanan.cr.core.action;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Freeman
 */
public class Add {

    private final List<String> coordinates = new ArrayList<>();

    private Add() {}

    public List<String> coordinates() {
        return List.copyOf(coordinates);
    }

    public static Add of(String... coordinates) {
        Add add = new Add();
        add.coordinates.addAll(List.of(coordinates));
        return add;
    }
}
