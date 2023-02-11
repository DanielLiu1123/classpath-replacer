package com.freemanan.cr.core.anno;

import com.freemanan.cr.core.junit5.ClasspathReplacerExtension;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Freeman
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(ClasspathReplacerExtension.class)
public @interface ClasspathExclude {
    String[] value();
}
