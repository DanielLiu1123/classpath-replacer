package com.freemanan.cr.core.action;

import static com.freemanan.cr.core.anno.Verb.ADD;
import static com.freemanan.cr.core.anno.Verb.EXCLUDE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import org.junit.jupiter.api.Test;

/**
 * {@link Exclude} tester.
 */
class RecursiveExcludeTest {

    @Test
    @ClasspathReplacer(
            value = {
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.0"),
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
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.0"),
                @Action(verb = EXCLUDE, value = "org.springframework.cloud:spring-cloud-starter-bootstrap"),
            },
            recursiveExclude = true)
    void notExcludeSubDependencies_whenUsingMavenCoordinateWithoutVersion() {
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
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.0"),
                @Action(verb = EXCLUDE, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.0"),
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
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.0"),
                @Action(verb = EXCLUDE, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.0"),
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
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.1"),
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.0"),
                @Action(verb = EXCLUDE, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.0"),
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
}
