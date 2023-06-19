package con.freemanan.cr.junit5;

import static com.freemanan.cr.core.anno.Verb.ADD;
import static com.freemanan.cr.core.anno.Verb.EXCLUDE;
import static com.freemanan.cr.core.anno.Verb.OVERRIDE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import org.junit.jupiter.api.Test;

@ClasspathReplacer({
    @Action(verb = ADD, value = "org.springframework.boot:spring-boot:2.7.0"),
    @Action(verb = OVERRIDE, value = "org.springframework.boot:spring-boot:3.0.0"),
    @Action(verb = EXCLUDE, value = "spring-boot-3.0.0.jar"),
})
class ExcludeTests {

    private static String getSpringBootVersion() throws Exception {
        Class<?> sbv = Class.forName("org.springframework.boot.SpringBootVersion");
        return (String) sbv.getDeclaredMethod("getVersion").invoke(null);
    }

    @Test
    void testSpringBootHasBeenOverride() throws Exception {
        String version = getSpringBootVersion();
        assertNotEquals("3.0.0", version);
        assertEquals("2.7.0", version);
    }

    @Test
    @ClasspathReplacer({
        @Action(verb = ADD, value = "org.springframework.boot:spring-boot:2.7.0"),
        @Action(verb = OVERRIDE, value = "org.springframework.boot:spring-boot:3.0.0"),
        @Action(verb = EXCLUDE, value = "org.springframework.boot:spring-boot:3.0.0"),
    })
    void exclude_whenUsingCoordinate() throws Exception {
        String version = getSpringBootVersion();
        assertNotEquals("3.0.0", version);
        assertEquals("2.7.0", version);
    }

    @Test
    @ClasspathReplacer({
        @Action(verb = ADD, value = "org.springframework.boot:spring-boot:2.7.0"),
        @Action(verb = OVERRIDE, value = "org.springframework.boot:spring-boot:3.0.0"),
        @Action(verb = EXCLUDE, value = "org.springframework.boot:spring-boot"),
    })
    void exclude_whenUsingCoordinateWithNoVersion() {
        assertThrows(ClassNotFoundException.class, ExcludeTests::getSpringBootVersion);
    }
}
