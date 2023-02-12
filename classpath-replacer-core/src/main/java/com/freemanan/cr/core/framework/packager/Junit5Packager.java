package com.freemanan.cr.core.framework.packager;

/**
 * @author Freeman
 */
public class Junit5Packager implements Packager {
    @Override
    public String[] internalPackages() {
        return new String[] {
            "org.junit",
        };
    }
}
