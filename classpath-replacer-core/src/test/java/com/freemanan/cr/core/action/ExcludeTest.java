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
class ExcludeTest {

    @Test
    @ClasspathReplacer({
        @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
        @Action(
                verb = EXCLUDE,
                value = "org.springframework:spring-cloud-starter-bootstrap:3.1.5"), // different group id
    })
    void testKnownIssue_whenMavenProjectHasDifferentGroupId_thenExcludePossibly() {
        // TODO: known issue, shouldn't not exclude when different group id
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
    }

    @Test
    @ClasspathReplacer({
        @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
        @Action(verb = EXCLUDE, value = "org.spring:spring-cloud-starter-bootstrap:3.1.5"), // different group id
    })
    void notExclude_whenDifferentGroupId() {
        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });
    }
}
