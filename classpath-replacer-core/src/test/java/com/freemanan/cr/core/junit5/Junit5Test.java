package com.freemanan.cr.core.junit5;

import static com.freemanan.cr.core.anno.Action.Verb.ADD;
import static com.freemanan.cr.core.anno.Action.Verb.EXCLUDE;
import static com.freemanan.cr.core.anno.Action.Verb.OVERRIDE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * @author Freeman
 */
@ClasspathReplacer({
    @Action(verb = ADD, value = "org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5"),
    @Action(verb = ADD, value = "com.google.code.gson:gson:2.8.9"),
    @Action(verb = OVERRIDE, value = "com.google.code.gson:gson:2.9.0"),
    @Action(verb = EXCLUDE, value = "gson-2.9.0.jar"),
})
class Junit5Test {

    @Test
    void testClasspathReplacer() {

        assertDoesNotThrow(() -> {
            Class.forName("org.springframework.cloud.bootstrap.marker.Marker");
        });

        assertThrows(InvocationTargetException.class, () -> {
            // Gson 2.8.9 has a bug
            // new Gson().toJson(Collections.emptyList()) will throw InvocationTargetException when using JDK 17
            // 2.9.0 has been fixed
            Class<?> gson = Class.forName("com.google.gson.Gson");
            Method toJson = gson.getMethod("toJson", Object.class);
            Constructor<?> constructor = gson.getDeclaredConstructor();
            Object obj = constructor.newInstance();
            toJson.invoke(obj, Collections.emptyList());
        });

        assertDoesNotThrow(() -> {
            Class<?> sbv = Class.forName("org.springframework.boot.SpringBootVersion");
            Object version = sbv.getDeclaredMethod("getVersion").invoke(null);
            assertEquals("2.6.13", version);
        });
    }
}
