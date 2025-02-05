package cr.framework.packager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Freeman
 */
public class PackagerHolder {

    private static final List<Packager> packagers = new ArrayList<>();

    static {
        if (isPresent("org.junit.jupiter.api.Test")) {
            packagers.add(new Junit5Packager());
        }
        if (isPresent("org.hamcrest.Matcher")) {
            packagers.add(new HamcrestPackager());
        }
        if (isPresent("org.opentest4j.AssertionFailedError")) {
            packagers.add(new Opentest4JPackager());
        }
        if (isPresent("org.assertj.core.api.Assertions")) {
            packagers.add(new AssertJPackager());
        }
        if (isPresent("org.mockito.Mockito")) {
            packagers.add(new MockitoPackager());
        }
    }

    /**
     * Get all {@link Packager}s, those packages should not be loaded with modified class loader.
     *
     * @return all packagers
     */
    public static List<Packager> getPackagers() {
        return Collections.unmodifiableList(packagers);
    }

    private static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
