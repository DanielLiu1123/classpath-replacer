package con.freemanan.cr.junit5;

import static com.freemanan.cr.core.anno.Verb.ADD;
import static com.freemanan.cr.core.anno.Verb.EXCLUDE;
import static com.freemanan.cr.core.anno.Verb.OVERRIDE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import org.junit.jupiter.api.Test;

@ClasspathReplacer({
    @Action(verb = ADD, value = "org.springframework.boot:spring-boot:2.7.0"),
    @Action(verb = OVERRIDE, value = "org.springframework.boot:spring-boot:2.7.1"),
    @Action(verb = EXCLUDE, value = "spring-boot-2.7.1.jar"),
})
class ClasspathReplacerMethodLevelTests {

    @Test
    void testVersionIs270_whenAdd270ThenOverrideWith300ThenExclude300() throws Exception {
        Class<?> sbv = Class.forName("org.springframework.boot.SpringBootVersion");
        Object version = sbv.getDeclaredMethod("getVersion").invoke(null);
        assertNotEquals("2.7.1", version);
        assertEquals("2.7.0", version);
    }

    @Test
    @ClasspathReplacer({
        @Action(verb = ADD, value = "org.springframework.boot:spring-boot:2.7.1"),
    })
    void testVersionIs300_whenAdd300() throws Exception {
        Class<?> sbv = Class.forName("org.springframework.boot.SpringBootVersion");
        Object version = sbv.getDeclaredMethod("getVersion").invoke(null);
        assertNotEquals("2.7.0", version);
        assertEquals("2.7.1", version);
    }
}
