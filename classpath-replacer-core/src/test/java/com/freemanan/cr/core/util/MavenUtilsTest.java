package com.freemanan.cr.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.freemanan.cr.core.anno.ClasspathReplacer;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.Test;

/** {@link com.freemanan.cr.core.util.MavenUtils} tester. */
class MavenUtilsTest {

    /**
     * {@link com.freemanan.cr.core.util.MavenUtils#resolveCoordinates(String[], ClasspathReplacer)}
     */
    @Test
    void resolveCoordinates() {
        String[] arr = {"com.google.code.gson:gson:2.8.9"};
        List<URL> urls = MavenUtils.resolveCoordinates(arr, null);
        assertEquals(1, urls.size());

        // With transitive dependencies
        //        arr = new String[] {"org.springframework.boot:spring-boot-starter:3.2.0"};
        arr = new String[] {"org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"};
        urls = MavenUtils.resolveCoordinates(arr, null);
        assertTrue(urls.size() > 1);
    }
}
