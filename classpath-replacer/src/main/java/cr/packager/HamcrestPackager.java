package cr.packager;

/**
 * @author Freeman
 */
public class HamcrestPackager implements Packager {
    @Override
    public String[] internalPackages() {
        return new String[] {"org.hamcrest"};
    }
}
