package con.freemanan.cr.junit5;

import static com.freemanan.cr.core.anno.Action.Type.ADD;
import static com.freemanan.cr.core.anno.Action.Type.OVERRIDE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import org.junit.jupiter.api.Test;

/**
 * @author Freeman
 */
@ClasspathReplacer({
    @Action(action = ADD, value = "org.springframework.boot:spring-boot:2.7.0"),
    @Action(action = OVERRIDE, value = "org.springframework.boot:spring-boot:3.0.0"),
})
public class OverrideTests {

    @Test
    void testSpringBootHasBeenOverride() throws Exception {
        Class<?> sbv = Class.forName("org.springframework.boot.SpringBootVersion");
        Object version = sbv.getDeclaredMethod("getVersion").invoke(null);
        assertEquals("3.0.0", version);
    }
}