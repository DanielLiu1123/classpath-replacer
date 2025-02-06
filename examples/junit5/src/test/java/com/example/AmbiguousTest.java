package com.example;

import static org.junit.jupiter.api.Assertions.assertThrows;

import cr.Classpath;
import org.junit.jupiter.api.Test;

/**
 * @author Freeman
 */
public class AmbiguousTest {

    @Test
    @Classpath(
            add = {
                "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.4",
                "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"
            },
            exclude = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5",
            excludeTransitive = true)
    void testRecursiveExclude_whenDifferentJarsHaveSameDependency_thenShouldRemoveTheDependency() {
        // spring-cloud-starter-bootstrap 3.1.4/3.1.5 both have spring-security-rsa-1.0.11.RELEASE
        // when recursive exclude spring-cloud-starter-bootstrap 3.1.5, spring-security-rsa-1.0.11.RELEASE will be
        // excluded
        assertThrows(
                ClassNotFoundException.class,
                () -> Class.forName("org.springframework.security.rsa.crypto.RsaAlgorithm"));
    }
}
