package cr;

import cr.util.Const;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * @author Freeman
 */
final class MavenUtils {
    private static final int MAX_RESOLUTION_ATTEMPTS = 3;

    private MavenUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Resolves Maven coordinate to a list of URLs.
     *
     * @param coordinate Maven coordinates of the form groupId:artifactId:version
     * @return list of URLs to the resolved artifacts
     */
    public static List<URL> resolveCoordinate(String coordinate) {
        if (coordinate == null || coordinate.isEmpty()) {
            throw new IllegalArgumentException("Coordinate cannot be null or empty");
        }
        if (!Pattern.matches(Const.MAVEN_COORDINATE_PATTERN, coordinate)) {
            throw new IllegalArgumentException("Invalid Maven coordinate: " + coordinate);
        }
        Exception latestFailure = null;
        for (int i = 0; i < MAX_RESOLUTION_ATTEMPTS; i++) {
            try {
                File[] dependencies =
                        Maven.resolver().resolve(coordinate).withTransitivity().asFile();
                List<URL> result = new ArrayList<>();
                for (File dependency : dependencies) {
                    result.add(dependency.toURI().toURL());
                }
                return result;
            } catch (Exception ex) {
                latestFailure = ex;
            }
        }
        throw new IllegalStateException(
                "Resolution failed after " + MAX_RESOLUTION_ATTEMPTS + " attempts", latestFailure);
    }
}
