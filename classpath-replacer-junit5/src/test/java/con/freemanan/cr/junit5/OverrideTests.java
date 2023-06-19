package con.freemanan.cr.junit5;

import static com.freemanan.cr.core.anno.Verb.ADD;
import static com.freemanan.cr.core.anno.Verb.OVERRIDE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import org.junit.jupiter.api.Test;

/**
 * @author Freeman
 */
@ClasspathReplacer({
    @Action(verb = ADD, value = "org.springframework.boot:spring-boot:2.7.0"),
    @Action(verb = OVERRIDE, value = "org.springframework.boot:spring-boot:2.7.1"),
})
class OverrideTests {

    @Test
    void testSpringBootHasBeenOverride() throws Exception {
        Class<?> sbv = Class.forName("org.springframework.boot.SpringBootVersion");
        Object version = sbv.getDeclaredMethod("getVersion").invoke(null);
        assertEquals("2.7.1", version);
    }
}
