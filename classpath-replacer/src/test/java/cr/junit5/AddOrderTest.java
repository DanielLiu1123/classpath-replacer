package cr.junit5;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cr.Classpath;
import org.junit.jupiter.api.Test;

/**
 * @author Freeman
 */
class AddOrderTest {

    @Test
    @Classpath(add = {"org.springframework.boot:spring-boot:2.7.0", "org.springframework.boot:spring-boot:2.7.1"})
    void versionShouldBe3_0_1_whenAdd3_0_0_thenAdd3_0_1() {
        assertEquals("2.7.1", springBootVersion());
    }

    @Test
    @Classpath(add = {"org.springframework.boot:spring-boot:2.7.1", "org.springframework.boot:spring-boot:2.7.0"})
    void versionShouldBe3_0_0_whenAdd3_0_1_thenAdd3_0_0() {
        assertEquals("2.7.0", springBootVersion());
    }

    private static String springBootVersion() {
        try {
            Class<?> sbv = Class.forName("org.springframework.boot.SpringBootVersion");
            Object version = sbv.getDeclaredMethod("getVersion").invoke(null);
            return (String) version;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
