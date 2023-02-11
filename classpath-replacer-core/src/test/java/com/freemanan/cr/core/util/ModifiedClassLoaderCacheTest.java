package com.freemanan.cr.core.util;

import static com.freemanan.cr.core.anno.Action.Verb.ADD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * {@link ModifiedClassLoaderCache} tester.
 *
 * @author Freeman
 */
@ClasspathReplacer({
    @Action(verb = ADD, value = "com.google.code.gson:gson:2.9.0"),
})
class ModifiedClassLoaderCacheTest {

    private static final Set<ClassLoader> classLoaders = new HashSet<>();

    @Test
    @Order(1)
    void test1() {
        // different class loader
        assertNull(ModifiedClassLoaderCache.get(ModifiedClassLoaderCacheTest.class));

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assertEquals(ModifiedClassLoaderCacheTest.class.getClassLoader(), classLoader);

        classLoaders.add(classLoader);
        assertSame(1, classLoaders.size());
    }

    @Test
    @Order(2)
    void test2() {
        // different class loader
        assertNull(ModifiedClassLoaderCache.get(ModifiedClassLoaderCacheTest.class));

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (!classLoaders.isEmpty()) {
            assertSame(classLoaders.iterator().next(), classLoader);
            classLoaders.add(classLoader);
        }
    }

    @Test
    @Order(3)
    @ClasspathReplacer({
        @Action(verb = ADD, value = "com.google.code.gson:gson:2.9.0"),
    })
    void test3() {
        // different class loader
        assertNull(ModifiedClassLoaderCache.get(ModifiedClassLoaderCacheTest.class));

        ClassLoader classLoader = ModifiedClassLoaderCacheTest.class.getClassLoader();
        if (!classLoaders.isEmpty()) {
            assertNotSame(classLoaders.iterator().next(), classLoader);
            assertFalse(classLoaders.contains(classLoader));

            classLoaders.add(classLoader);
        }
    }

    @Test
    @Order(4)
    @ClasspathReplacer({
        @Action(verb = ADD, value = "com.google.code.gson:gson:2.9.0"),
    })
    void test4() {
        // different class loader
        assertNull(ModifiedClassLoaderCache.get(ModifiedClassLoaderCacheTest.class));

        ClassLoader classLoader = ModifiedClassLoaderCacheTest.class.getClassLoader();
        if (!classLoaders.isEmpty()) {
            assertFalse(classLoaders.contains(classLoader));
        }
    }
}
