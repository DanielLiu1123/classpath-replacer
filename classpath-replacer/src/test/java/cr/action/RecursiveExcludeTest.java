package cr.action;

import static cr.anno.Verb.ADD;
import static cr.anno.Verb.EXCLUDE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cr.anno.Action;
import cr.anno.ClasspathReplacer;
import org.junit.jupiter.api.Test;

/**
 * {@link Exclude} tester.
 */
class RecursiveExcludeTest {

    @Test
    @ClasspathReplacer(
            value = {
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
                @Action(verb = EXCLUDE, value = "spring-cloud-starter-bootstrap-*.jar"),
            },
            recursiveExclude = true)
    void notExcludeSubDependencies_whenUsingJarFileName() {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
    }

    @Test
    @ClasspathReplacer(
            value = {
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
                @Action(verb = EXCLUDE, value = "org.springframework.cloud:spring-cloud-starter-bootstrap"),
            },
            recursiveExclude = true)
    void excludeSubDependencies_whenUsingMavenCoordinateWithoutVersion() {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
    }

    @Test
    @ClasspathReplacer(
            value = {
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
                @Action(verb = EXCLUDE, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
            },
            recursiveExclude = true)
    void excludeSubDependencies_whenUsingMavenCoordinate() {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
    }

    @Test
    @ClasspathReplacer(
            value = {
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
                @Action(verb = EXCLUDE, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
            },
            recursiveExclude = false)
    void notExcludeSubDependencies_whenRecursiveExcludeDisabled() {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
    }

    @Test
    @ClasspathReplacer(
            value = {
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.6"),
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
                @Action(verb = EXCLUDE, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
            },
            recursiveExclude = true)
    void excludeSubDependencies_whenHasDifferentVersion_thenOnlyCurrentVersionIsExcluded() {
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
    }

    @Test
    @ClasspathReplacer(
            value = {
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.6"),
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
                @Action(verb = EXCLUDE, value = "org.springframework.cloud:spring-cloud-starter-bootstrap"),
            },
            recursiveExclude = true)
    void excludeSubDependencies_whenRecursiveExcludeWithoutVersion() {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
        // If dependencies with different versions have same version dependency, still works perfectly ^_*
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.security.rsa.crypto.RsaAlgorithm");
        });
    }

    @Test
    @ClasspathReplacer(
            value = {
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.6"),
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
                @Action(verb = EXCLUDE, value = "org.springframework.cloud:spring-cloud-starter-bootstrap"),
            },
            recursiveExclude = false)
    void excludeAllVersionOfThisDependency_whenHaveSameDependencyWithDifferentVersionAndRecursiveExcludeDisabled() {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
    }
}
