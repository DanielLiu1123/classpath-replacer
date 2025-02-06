package cr.packager;

/**
 * @author Freeman
 */
public class AssertJPackager implements Packager {
    @Override
    public String[] internalPackages() {
        return new String[] {"org.assertj"};
    }
}
