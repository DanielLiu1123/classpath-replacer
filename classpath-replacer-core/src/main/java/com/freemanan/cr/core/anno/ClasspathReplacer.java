package com.freemanan.cr.core.anno;

import com.freemanan.cr.core.framework.junit5.ClasspathReplacerExtension;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@link ClasspathReplacer} is used to replace the classpath with the given actions.
 *
 * <p>
 * This operation is performed in order, so if there are multiple operations, you need to pay attention to the order.
 *
 * <p>
 * For example:
 * <pre>{@code
 * ClasspathReplacer({
 *     @Action(action = EXCLUDE, value = "slf4j-log4j12-*.jar"),
 *     @Action(action = ADD, value = "ch.qos.logback:logback-classic:1.4.5")
 * })
 * class SomeTest {}
 * }</pre>
 * <p> This will exclude the {@code slf4j-log4j12} first then add the {@code logback-classic} jar.
 *
 * <p>
 * When add new dependencies, there may be dependency conflicts. For example, the log framework used by the current program is {@code logback}, and the added dependency using {@code log4j}.
 * Therefore, in complex scenarios, the ability to define the order of actions is a very important feature.
 *
 * @author Freeman
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendWith(ClasspathReplacerExtension.class)
public @interface ClasspathReplacer {
    /**
     * Replace the classpath with the given actions.
     *
     * @return the actions
     */
    Action[] value();

    /**
     * Extra repositories to use when resolving dependencies.
     *
     * <p> Default repository is maven central.
     *
     * @return extra repositories
     */
    Repository[] repositories() default {};

    /**
     * Whether to exclude the sub-dependencies of the excluded dependency.
     *
     * <p> Only works on {@link Verb#EXCLUDE} action, and the value must be a Maven coordinate (version is optional).
     *
     * <p> For example: both {@code com.google.code.gson:gson:2.8.9} and {@code com.google.code.gson:gson} are allowed, if the version is not specified, will exclude all versions of the dependency and its sub-dependencies.
     *
     * @return whether to exclude the sub-dependencies of the excluded dependency.
     */
    boolean recursiveExclude() default false;
}
