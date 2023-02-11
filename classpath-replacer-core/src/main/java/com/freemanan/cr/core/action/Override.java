package com.freemanan.cr.core.action;

import com.freemanan.cr.core.util.Const;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Freeman
 */
public class Override {
    private static final Logger log = LoggerFactory.getLogger(Override.class);

    private final List<String> coordinates = new ArrayList<>();

    private Override() {}

    public List<String> coordinates() {
        return List.copyOf(coordinates);
    }

    public static Override of(String... coordinates) {
        List<String> validCoordinates = new ArrayList<>();
        for (String coordinate : coordinates) {
            if (coordinate.matches(Const.MAVEN_COORDINATE_WITH_VERSION_PATTERN)) {
                validCoordinates.add(coordinate);
            } else {
                log.warn("Invalid maven coordinate: {}", coordinate);
            }
        }

        Override override = new Override();
        override.coordinates.addAll(List.copyOf(validCoordinates));
        return override;
    }
}
