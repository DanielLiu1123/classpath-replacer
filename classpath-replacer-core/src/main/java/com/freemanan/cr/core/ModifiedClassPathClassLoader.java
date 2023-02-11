package com.freemanan.cr.core;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Freeman
 */
public class ModifiedClassPathClassLoader extends URLClassLoader {

    private final ClassLoader appClassLoader;

    public ModifiedClassPathClassLoader(URL[] urls, ClassLoader parent, ClassLoader appClassLoader) {
        super(urls, parent);
        this.appClassLoader = appClassLoader;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith("org.junit") || name.startsWith("org.hamcrest")) {
            return Class.forName(name, false, this.appClassLoader);
        }
        return super.loadClass(name);
    }
}
