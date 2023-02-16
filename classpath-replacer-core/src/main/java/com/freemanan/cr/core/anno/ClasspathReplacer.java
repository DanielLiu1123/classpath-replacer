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
     * Whether to exclude the sub-dependencies of the excluded dependency.
     *
     * <p> Only works on {@link Verb#EXCLUDE} action, and the value must be a Maven coordinate with version.
     *
     * <p> For example: {@code org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.0} is allowed, but {@code org.springframework.cloud:spring-cloud-starter-bootstrap} is not allowed.
     *
     * @return whether to exclude the sub-dependencies of the excluded dependency.
     */
    boolean recursiveExclude() default false;
}
