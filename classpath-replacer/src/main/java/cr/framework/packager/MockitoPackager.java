package cr.framework.packager;

/**
 * @author Freeman
 */
public class MockitoPackager implements Packager {
    @Override
    public String[] internalPackages() {
        return new String[] {"org.mockito"};
    }
}
