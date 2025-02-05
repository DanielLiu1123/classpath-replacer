package com.example;

import static cr.anno.Verb.ADD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cr.anno.Action;
import cr.anno.ClasspathReplacer;
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
}
