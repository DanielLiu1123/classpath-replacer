package com.example;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cr.Classpath;
import org.junit.jupiter.api.Test;

/**
 * <p> Gradle project jars are cached in ~/.gradle/caches/modules-2/files-2.1
 * <p> We use maven api to get the dependencies, so the same jar will be cached in different places.
 *
 * @author Freeman
 */
class GradleProjectTest {

    // PulsarClient is in pulsar-client-api

    @Test
    @Classpath(exclude = "org.apache.pulsar:pulsar-client:2.11.0")
    void noException_whenExcludePulsarClient() {
        assertDoesNotThrow(() -> Class.forName("org.apache.pulsar.client.api.PulsarClient"));
    }

    @Test
    @Classpath(exclude = "org.apache.pulsar:pulsar-client")
    void noException_whenExcludeAllVersionsOfPulsarClient() {
        // shouldn't exclude pulsar-client-api
        assertDoesNotThrow(() -> Class.forName("org.apache.pulsar.client.api.PulsarClient"));
    }

    @Test
    @Classpath(exclude = "org.apache.pulsar:pulsar-client-api:2.11.0")
    void classNotFound_whenExcludePulsarClientApi() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.apache.pulsar.client.api.PulsarClient"));
    }

    @Test
    @Classpath(exclude = "org.apache.pulsar:pulsar-client-api")
    void classNotFound_whenExcludeAllVersionsOfPulsarClientApi() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.apache.pulsar.client.api.PulsarClient"));
    }

    @Test
    @Classpath(exclude = "org.apache.pulsar:pulsar-client:2.11.0", excludeTransitive = true)
    void classNotFound_whenRecursiveExcludePulsarClient() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.apache.pulsar.client.api.PulsarClient"));
    }

    @Test
    @Classpath(exclude = "org.apache.pulsar:pulsar-client", excludeTransitive = true)
    void classNotFound_whenRecursiveExcludeAllVersionsOfPulsarClient() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.apache.pulsar.client.api.PulsarClient"));
    }
}
