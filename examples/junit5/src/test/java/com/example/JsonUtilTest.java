package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cr.Classpath;
import org.junit.jupiter.api.Test;

class JsonUtilTest {

    @Test
    void testNoJsonImplementationOnClasspath() {
        assertThrows(ExceptionInInitializerError.class, JsonUtil::instance);
    }

    @Test
    @Classpath(add = "com.google.code.gson:gson:2.10.1")
    void testGsonOnClasspath() {
        assertTrue(JsonUtil.instance() instanceof Gson);
        assertEquals("{}", JsonUtil.toJson(new Object()));
    }

    @Test
    @Classpath(add = "com.fasterxml.jackson.core:jackson-databind:2.14.1")
    void testJacksonOnClasspath() {
        assertTrue(JsonUtil.instance() instanceof Jackson);
    }

    @Test
    @Classpath(add = {"com.fasterxml.jackson.core:jackson-databind:2.14.1", "com.google.code.gson:gson:2.10.1"})
    void useJacksonFirst_whenBothJacksonAndGsonOnClasspath() {
        assertTrue(JsonUtil.instance() instanceof Jackson);
    }
}
