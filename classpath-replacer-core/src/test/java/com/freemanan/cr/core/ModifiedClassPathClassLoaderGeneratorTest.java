package com.freemanan.cr.core;

import static org.junit.jupiter.api.Assertions.*;

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
        String[] arr = {"com.google.code.gson:gson:2.8.9"};
        List<URL> urls = ModifiedClassPathClassLoaderGenerator.resolveCoordinates(arr, null);
        assertEquals(1, urls.size());
    }
}
