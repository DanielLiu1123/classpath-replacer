package cr.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cr.Classpath;
import org.junit.jupiter.api.Test;

/**
 * {@link Exclude} tester.
 */
class ExcludeTest {

    @Test
    @Classpath(
            add = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5",
            exclude = "org.springframework:spring-cloud-starter-bootstrap:3.1.5" // different group id
            )
    void testKnownIssue_whenMavenProjectHasDifferentGroupId_thenExcludePossibly() {
        // TODO: known issue, shouldn't not exclude when different group id
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
    }

    @Test
    @Classpath(
            add = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5",
            exclude = "org.spring:spring-cloud-starter-bootstrap:3.1.5" // different group id
            )
    void notExclude_whenDifferentGroupId() {
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
    }

    @Test
    @Classpath(
            add = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5",
            exclude = "org.spring:spring-cloud-starter-bootstrap" // different group id
            )
    void notExclude_whenDifferentGroupIdWithoutVersion() {
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
    }

    @Test
    @Classpath(
            add = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5",
            exclude = "org.spring:spring-cloud-starter-bootstrap", // different group id
            excludeTransitive = true)
    void notExclude_whenDifferentGroupIdWithRecursiveExclude() {
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
    }

    @Test
    void testIllegalPatterns() {
        assertThrows(IllegalArgumentException.class, () -> Exclude.of("gson-*"));
    }
}
