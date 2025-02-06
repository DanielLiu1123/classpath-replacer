package cr.junit5;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import cr.Classpath;
import org.junit.jupiter.api.Test;

/**
 * @author Freeman
 */
@Classpath(
        add = {
            "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5",
            "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.6"
        },
        exclude = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.6",
        excludeTransitive = true)
class Junit5Test {

    @Test
    void testClasspathReplacer() {

        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });

        assertDoesNotThrow(() -> {
            Class<?> sbv = Class.forName("org.springframework.boot.SpringBootVersion");
            Object version = sbv.getDeclaredMethod("getVersion").invoke(null);
            assertEquals("2.6.13", version);
            assertNotEquals("2.6.14", version);
        });
    }
}
