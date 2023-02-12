package com.example;

import static com.freemanan.cr.core.anno.Verb.ADD;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import java.lang.reflect.InaccessibleObjectException;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class JsonUtilTest {

    @Test
    void testNoJsonImplementationOnClasspath() {
        assertThrows(ExceptionInInitializerError.class, JsonUtil::instance);
    }

    @Test
    @ClasspathReplacer(@Action(verb = ADD, value = "com.google.code.gson:gson:2.10.1"))
    void testGsonOnClasspath() {
        assertTrue(JsonUtil.instance() instanceof Gson);
        assertEquals("{}", JsonUtil.toJson(new Object()));
    }

    @Test
    @ClasspathReplacer(@Action(verb = ADD, value = "com.fasterxml.jackson.core:jackson-databind:2.14.1"))
    void testJacksonOnClasspath() {
        assertTrue(JsonUtil.instance() instanceof Jackson);
    }

    @Test
    @ClasspathReplacer({
        @Action(verb = ADD, value = "com.fasterxml.jackson.core:jackson-databind:2.14.1"),
        @Action(verb = ADD, value = "com.google.code.gson:gson:2.10.1")
    })
    void useJacksonFirst_whenBothJacksonAndGsonOnClasspath() {
        assertTrue(JsonUtil.instance() instanceof Jackson);
    }

    @Test
    @ClasspathReplacer({@Action(verb = ADD, value = "com.google.code.gson:gson:2.8.9")})
    void gsonHasBugOn2_8_9_whenEmptyList() {
        assertThrows(InaccessibleObjectException.class, () -> {
            JsonUtil.toJson(Collections.emptyList());
        });
    }

    @Test
    @ClasspathReplacer({@Action(verb = ADD, value = "com.google.code.gson:gson:2.9.0")})
    void gsonWorksFineOn2_9_0_whenEmptyList() {
        assertDoesNotThrow(() -> {
            JsonUtil.toJson(Collections.emptyList());
        });
    }
}
