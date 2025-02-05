package cr.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Repository annotation.
 *
 * @author Freeman
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Repository {

    /**
     * Repository url.
     *
     * <p> e.g. <a href="https://repo1.maven.org/maven2/">https://repo1.maven.org/maven2/</a>
     *
     * @return the repository url
     */
    String value();

    /**
     * Repository id, optional.
     *
     * @return the repository id
     */
    String id() default "";

    /**
     * Repository authentication username if needed.
     *
     * @return the username
     */
    String username() default "";

    /**
     * Repository authentication password if needed.
     *
     * @return the password
     */
    String password() default "";
}
