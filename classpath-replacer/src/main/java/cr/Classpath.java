package cr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@link Classpath} is used to replace the classpath with the given actions.
 *
 * <p> Examples:
 *
 * <p> Add the {@code logback-classic} jar and exclude the {@code slf4j-log4j12} and
 * <pre>{@code
 * @Classpath(
 *     add = "ch.qos.logback:logback-classic:1.4.5",
 *     exclude = "slf4j-log4j12-*.jar"
 * )
 * }</pre>
 *
 * <p> Exclude the {@code spring-boot-starter-web} and all its transitive dependencies:
 * <pre>{@code
 * @Classpath(
 *     exclude = "org.springframework.boot:spring-boot-starter-web",
 *     excludeTransitive = true
 * )
 * }</pre>
 *
 * <p> {@link Classpath} can be used on the class level or method level, and the method level will override the class level.
 *
 * <p> When both {@link #add()} and {@link #exclude()} are specified, the {@link #add()} will be executed first.
 *
 * @author Freeman
 * @since 3.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendWith(ClasspathExtension.class)
public @interface Classpath {

    /**
     * Add dependencies to the classpath.
     *
     * @return the dependencies to add.
     */
    String[] add() default {};

    /**
     * Exclude dependencies from the classpath.
     *
     * <p> The value must be a Maven coordinate (version is optional), if the version is not specified, will exclude all versions of the dependency.
     *
     * @return the dependencies to exclude.
     */
    String[] exclude() default {};

    /**
     * Whether to exclude the transitive dependencies.
     *
     * @return whether to exclude the transitive dependencies.
     */
    boolean excludeTransitive() default false;
}
