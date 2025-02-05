package cr.anno;

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

    /**
     * The values associated with the {@link #verb}.
     *
     * @return value
     */
    String[] value();

    /**
     * The verb of the action.
     *
     * @return {@link Verb}
     * @see Verb
     */
    Verb verb() default Verb.ADD;
}
