package com.freemanan.cr.core;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link ModifiedClassPathClassLoaderGenerator} tester.
 */
class ModifiedClassPathClassLoaderGeneratorTest {

    /**
     * {@link ModifiedClassPathClassLoaderGenerator#resolveCoordinates(String[])}
     */
    @Test
    void resolveCoordinates() {
        String[] arr = {
            "com.google.code.gson:gson:2.8.9", "org.springframework.cloud:spring-cloud-starter-openfeign:4.0.0",
        };
        List<URL> urls = ModifiedClassPathClassLoaderGenerator.resolveCoordinates(arr);
        urls.forEach(System.out::println);
    }
}
