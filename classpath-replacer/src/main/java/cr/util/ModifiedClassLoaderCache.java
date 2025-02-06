package cr.util;

import cr.ModifiedClassPathClassLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * @author Freeman
 */
public final class ModifiedClassLoaderCache {

    private static final ConcurrentMap<Class<?>, ModifiedClassPathClassLoader> cache = new ConcurrentHashMap<>();

    public static ModifiedClassPathClassLoader get(Class<?> testClass) {
        return cache.get(testClass);
    }

    /**
     * Get or put a {@link ModifiedClassPathClassLoader}.
     *
     * @param testClass test class
     * @param supplier supplier
     * @return existing or supplied {@link ModifiedClassPathClassLoader}
     */
    public static ModifiedClassPathClassLoader getOrPut(
            Class<?> testClass, Supplier<ModifiedClassPathClassLoader> supplier) {
        return cache.computeIfAbsent(testClass, k -> supplier.get());
    }
}
