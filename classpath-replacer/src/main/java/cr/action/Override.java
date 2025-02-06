package cr.action;

import cr.util.Const;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Freeman
 * @deprecated by Freeman since 3.0.0, use {@link Add} instead.
 */
@Deprecated
public final class Override {

    private final List<String> coordinates = new ArrayList<>();

    private Override() {}

    public List<String> coordinates() {
        return Collections.unmodifiableList(coordinates);
    }

    public static Override of(String... coordinates) {
        List<String> validCoordinates = new ArrayList<>();
        for (String coordinate : coordinates) {
            if (!coordinate.matches(Const.MAVEN_COORDINATE_WITH_VERSION_PATTERN)) {
                throw new IllegalArgumentException("Invalid maven coordinate: " + coordinate);
            }
            validCoordinates.add(coordinate);
        }

        Override override = new Override();
        override.coordinates.addAll(validCoordinates);
        return override;
    }
}
