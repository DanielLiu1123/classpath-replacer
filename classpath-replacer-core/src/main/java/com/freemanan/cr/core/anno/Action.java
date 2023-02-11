package com.freemanan.cr.core.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Freeman
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
// @Repeatable(ClasspathReplacer.class)
public @interface Action {

    String[] value();

    Type action() default Type.ADD;

    enum Type {
        /**
         * Add jar in the classpath.
         */
        ADD,
        /**
         * Replace jar in the classpath.
         */
        OVERRIDE,
        /**
         * Exclude jar in the classpath.
         */
        EXCLUDE
    }
}
