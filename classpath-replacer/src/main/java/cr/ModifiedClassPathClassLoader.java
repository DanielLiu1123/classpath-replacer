package cr;

import cr.packager.PackagerHolder;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Freeman
 */
public class ModifiedClassPathClassLoader extends URLClassLoader {

    private final ClassLoader appClassLoader;
    private final List<String> internalPackages;

    public ModifiedClassPathClassLoader(URL[] urls, ClassLoader parent, ClassLoader appClassLoader) {
        super(urls, parent);
        this.appClassLoader = appClassLoader;
        this.internalPackages = PackagerHolder.getPackagers().stream()
                .flatMap(packager -> Stream.of(packager.internalPackages()))
                .collect(Collectors.toList());
    }

    public static ModifiedClassPathClassLoaderBuilder builder(ClassLoader parent) {
        return new ModifiedClassPathClassLoaderBuilder(parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (isTestFrameworkInternalPackage(name)) {
            return Class.forName(name, false, this.appClassLoader);
        }
        return super.loadClass(name);
    }

    private boolean isTestFrameworkInternalPackage(String name) {
        for (String pkg : internalPackages) {
            if (name.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }
}
