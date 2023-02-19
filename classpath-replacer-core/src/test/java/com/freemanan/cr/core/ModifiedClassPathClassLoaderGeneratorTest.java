package com.freemanan.cr.core;

import com.freemanan.cr.core.anno.ClasspathReplacer;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link ModifiedClassPathClassLoaderGenerator} tester.
 */
class ModifiedClassPathClassLoaderGeneratorTest {

    /**
     * {@link ModifiedClassPathClassLoaderGenerator#resolveCoordinates(String[], ClasspathReplacer)}
     */
    @Test
    void resolveCoordinates() {
        String[] arr = {
            "com.google.code.gson:gson:2.8.9", "org.springframework.cloud:spring-cloud-starter-openfeign:4.0.0",
        };
        List<URL> urls = ModifiedClassPathClassLoaderGenerator.resolveCoordinates(arr, null);
        urls.forEach(System.out::println);
    }
}
