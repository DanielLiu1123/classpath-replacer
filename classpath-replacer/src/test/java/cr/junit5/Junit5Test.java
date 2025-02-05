package cr.junit5;

import static cr.anno.Verb.ADD;
import static cr.anno.Verb.EXCLUDE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import cr.anno.Action;
import cr.anno.ClasspathReplacer;
import org.junit.jupiter.api.Test;

/**
 * @author Freeman
 */
@ClasspathReplacer(
        value = {
            @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
            @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.6"),
            @Action(verb = EXCLUDE, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.6"),
        },
        recursiveExclude = true)
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
