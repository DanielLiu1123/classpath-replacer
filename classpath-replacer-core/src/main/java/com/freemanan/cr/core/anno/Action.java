package com.freemanan.cr.core.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * One action to the classpath.
 *
 * @author Freeman
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Action {

    String[] value();

    Verb verb() default Verb.ADD;

    enum Verb {
        /**
         * Add jar in the classpath.
         *
         * <p>
         * Must be a Maven coordinate, such as {@code com.fasterxml.jackson.core:jackson-databind:2.14.1}.
         * <p>
         * If the jar is already in the classpath, it will use the specific version first.
         */
        ADD,
        /**
         * Override jar in the classpath.
         *
         * <p>
         * Must be a Maven coordinate, such as {@code com.fasterxml.jackson.core:jackson-databind:2.14.1}.
         * <p>
         * If the jar is already in the classpath, it will use the specific version first.
         * <p>
         * NOTE: Override has the same effect as {@link #ADD}, Separate {@link #ADD} and OVERRIDE for clearer semantic expression.
         *
         * @see #ADD
         */
        OVERRIDE,
        /**
         * Exclude jar in the classpath.
         *
         * <p>
         * Jar file name and Maven coordinate are supported, such as {@code gson-2.8.9.jar} or {@code com.google.code.gson:gson:2.8.9}.
         * <p>
         * '*' is allowed when using file name, such as {@code gson*.jar}, will exclude all jars that start with {@code gson} on the classpath.
         * <p>
         * Maven coordinate version can be omitted, such as {@code com.google.code.gson:gson}, will exclude all versions of gson on the classpath.
         */
        EXCLUDE
    }
}
