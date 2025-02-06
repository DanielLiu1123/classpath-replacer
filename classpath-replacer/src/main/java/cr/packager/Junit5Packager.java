package cr.packager;

/**
 * @author Freeman
 */
public class Junit5Packager implements Packager {
    @Override
    public String[] internalPackages() {
        return new String[] {
            "org.junit",
        };
    }
}
