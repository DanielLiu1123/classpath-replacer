package cr.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cr.Classpath;
import org.junit.jupiter.api.Test;

/**
 * {@link Exclude} tester.
 */
class RecursiveExcludeTest {

    @Test
    @Classpath(
            add = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5",
            exclude = "spring-cloud-starter-bootstrap-*.jar",
            excludeTransitive = true)
    void notExcludeSubDependencies_whenUsingJarFileName() {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
    }

    @Test
    @Classpath(
            add = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5",
            exclude = "org.springframework.cloud:spring-cloud-starter-bootstrap",
            excludeTransitive = true)
    void excludeSubDependencies_whenUsingMavenCoordinateWithoutVersion() {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertThrows(NoClassDefFoundError.class, () -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
    }

    @Test
    @Classpath(
            add = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5",
            exclude = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5",
            excludeTransitive = true)
    void excludeSubDependencies_whenUsingMavenCoordinate() {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertThrows(NoClassDefFoundError.class, () -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
    }

    @Test
    @Classpath(
            add = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5",
            exclude = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5",
            excludeTransitive = false)
    void notExcludeSubDependencies_whenRecursiveExcludeDisabled() {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
    }

    @Test
    @Classpath(
            add = {
                "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.6",
                "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"
            },
            exclude = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5",
            excludeTransitive = true)
    void excludeSubDependencies_whenHasDifferentVersion_thenOnlyCurrentVersionIsExcluded() {
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
    }

    @Test
    @Classpath(
            add = {
                "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.6",
                "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"
            },
            exclude = "org.springframework.cloud:spring-cloud-starter-bootstrap",
            excludeTransitive = true)
    void excludeSubDependencies_whenRecursiveExcludeWithoutVersion() {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertThrows(NoClassDefFoundError.class, () -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
        // If dependencies with different versions have same version dependency, still works perfectly ^_*
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.security.rsa.crypto.RsaAlgorithm");
        });
    }

    @Test
    @Classpath(
            add = {
                "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.6",
                "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"
            },
            exclude = "org.springframework.cloud:spring-cloud-starter-bootstrap",
            excludeTransitive = false)
    void excludeAllVersionOfThisDependency_whenHaveSameDependencyWithDifferentVersionAndRecursiveExcludeDisabled() {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.boot.SpringApplication");
        });
    }
}
