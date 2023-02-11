package com.freemanan.cr.core.action;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Freeman
 */
public class Override {

    private final List<String> coordinates = new ArrayList<>();

    private Override() {}

    public List<String> coordinates() {
        return List.copyOf(coordinates);
    }

    public static Override of(String... coordinates) {
        Override override = new Override();
        override.coordinates.addAll(List.of(coordinates));
        return override;
    }
}
