package cr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Static field is not reusable when using {@link ClasspathReplacer}, it will be re-initialized.
 *
 * @author Freeman
 */
class StaticMethodTests {

    static AtomicInteger counter = new AtomicInteger(0);

    @Test
    @ClasspathReplacer({})
    void test1() {
        assertEquals(0, counter.getAndIncrement());
    }

    @Test
    @ClasspathReplacer({})
    void test12() {
        assertEquals(0, counter.getAndIncrement());
    }
}
