package cr.anno;

import cr.framework.junit5.ClasspathReplacerExtension;
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
 * <p> Examples:
 *
 * <p> Exclude the {@code slf4j-log4j12} first then add the {@code logback-classic} jar:
 * <pre>{@code
 * @ClasspathReplacer(
 *     value = {
 *         @Action(verb = EXCLUDE, value = "slf4j-log4j12-*.jar"),
 *         @Action(verb = ADD, value = "ch.qos.logback:logback-classic:1.4.5")
 *     }
 * )
 * }</pre>
 *
 * <p> Exclude the {@code spring-boot-starter-web} and all its sub-dependencies:
 * <pre>{@code
 * @ClasspathReplacer(
 *     value = {
 *         @Action(verb = EXCLUDE, value = "org.springframework.boot:spring-boot-starter-web")
 *     },
 *     recursiveExclude = true
 * )
 * }</pre>
 *
 * <p> Configure the extra repositories or proxy repository to use when resolving dependencies:
 * <pre>{@code
 * @ClasspathReplacer(
 *     value = {
 *         @Action(verb = ADD, value = "com.youcompany:your-dependency:1.0.0")
 *     },
 *     repositories = {
 *         @Repository(value = "https://maven.youcompany.com/repository/release/", username = "admin", password = "${MAVEN_PASSWORD}"),
 *         @Repository("https://maven.aliyun.com/repository/public/")
 *     }
 * )
 * }</pre>
 *
 * <p> When add new dependencies, there may be dependency conflicts. For example, the log framework used by the current program is {@code logback}, and the added dependency using {@code log4j}.
 * Therefore, in complex scenarios, the ability to define the order of actions is a very important feature.
 *
 * <p> {@link ClasspathReplacer} can be used on the class level or method level, and the method level will override the class level.
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
