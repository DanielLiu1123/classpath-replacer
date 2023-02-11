package con.freemanan.cr.junit5;

import static com.freemanan.cr.core.anno.Action.Type.ADD;
import static com.freemanan.cr.core.anno.Action.Type.EXCLUDE;
import static com.freemanan.cr.core.anno.Action.Type.OVERRIDE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import org.junit.jupiter.api.Test;

@ClasspathReplacer({
    @Action(action = ADD, value = "org.springframework.boot:spring-boot:2.7.0"),
    @Action(action = OVERRIDE, value = "org.springframework.boot:spring-boot:3.0.0"),
    @Action(action = EXCLUDE, value = "spring-boot-3.0.0.jar"),
})
public class ExcludeTests {

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
    void testSpringBootHasBeenOverride2() throws Exception {
        String version = getSpringBootVersion();
        assertNotEquals("3.0.0", version);
        assertEquals("2.7.0", version);
    }

    @Test
    @ClasspathReplacer({
        @Action(action = ADD, value = "org.springframework.boot:spring-boot:2.7.0"),
        @Action(action = OVERRIDE, value = "org.springframework.boot:spring-boot:3.0.0"),
        @Action(action = EXCLUDE, value = "org.springframework.boot:spring-boot:3.0.0"),
    })
    void exclude_whenUsingCoordinate() throws Exception {
        String version = getSpringBootVersion();
        assertNotEquals("3.0.0", version);
        assertEquals("2.7.0", version);
    }

    @Test
    @ClasspathReplacer({
        @Action(action = ADD, value = "org.springframework.boot:spring-boot:2.7.0"),
        @Action(action = OVERRIDE, value = "org.springframework.boot:spring-boot:3.0.0"),
        @Action(action = EXCLUDE, value = "org.springframework.boot:spring-boot"),
    })
    void exclude_whenUsingCoordinateWithNoVersion() {
        assertThrows(ClassNotFoundException.class, () -> {
            getSpringBootVersion();
        });
    }
}
