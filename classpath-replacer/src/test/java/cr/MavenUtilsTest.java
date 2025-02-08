package cr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link MavenUtils} tester.
 */
class MavenUtilsTest {

    /**
     * {@link MavenUtils#resolveCoordinate(String)}
     */
    @Test
    void testResolveCoordinate() {
        String gson = "com.google.code.gson:gson:2.8.9";
        List<URL> urls = MavenUtils.resolveCoordinate(gson);
        assertEquals(1, urls.size());

        String springCloudStarterBootstrap = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5";
        urls = MavenUtils.resolveCoordinate(springCloudStarterBootstrap);
        assertTrue(urls.size() > 1);
    }
}
