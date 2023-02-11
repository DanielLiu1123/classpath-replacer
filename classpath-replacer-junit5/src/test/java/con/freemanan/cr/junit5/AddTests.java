package con.freemanan.cr.junit5;

import static com.freemanan.cr.core.anno.Action.Type.ADD;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.freemanan.cr.core.ModifiedClassPathClassLoader;
import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import org.junit.jupiter.api.Test;

/**
 * @author Freeman
 */
@ClasspathReplacer({
    @Action(action = ADD, value = "com.google.code.gson:gson:2.9.0"),
})
public class AddTests {

    @Test
    void testMarkExists() {
        assertEquals(AddTests.class.getClassLoader(), Thread.currentThread().getContextClassLoader());
        assertEquals(
                ModifiedClassPathClassLoader.class.getName(),
                AddTests.class.getClassLoader().getClass().getName());

        assertDoesNotThrow(() -> {
            Class.forName("com.google.gson.Gson");
        });
    }
}
