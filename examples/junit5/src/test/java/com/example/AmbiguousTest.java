package com.example;

import static com.freemanan.cr.core.anno.Verb.ADD;
import static com.freemanan.cr.core.anno.Verb.EXCLUDE;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import org.junit.jupiter.api.Test;

/**
 * @author Freeman
 */
public class AmbiguousTest {

    @Test
    @ClasspathReplacer(
            value = {
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.4"),
                @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
                @Action(verb = EXCLUDE, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
            },
            recursiveExclude = true)
    void testRecursiveExclude_whenDifferentJarsHaveSameDependency_thenShouldRemoveTheDependency() {
        // spring-cloud-starter-bootstrap 3.1.4/3.1.5 both have spring-security-rsa-1.0.11.RELEASE
        // when recursive exclude spring-cloud-starter-bootstrap 3.1.5, spring-security-rsa-1.0.11.RELEASE will be
        // excluded
        assertThrows(
                ClassNotFoundException.class,
                () -> Class.forName("org.springframework.security.rsa.crypto.RsaAlgorithm"));
    }
}
