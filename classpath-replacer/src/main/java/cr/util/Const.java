package cr.util;

import cr.action.Exclude;

/**
 * @author Freeman
 */
public class Const {

    public static final String JAR_FILE_NAME_PATTERN = "^.+\\.jar$";

    public static final String MAVEN_COORDINATE_PATTERN = "^[^:]+:[^:]+(:[^:]+)?$";

    public static final String MAVEN_COORDINATE_WITH_VERSION_PATTERN = "^[^:]+:[^:]+:[^:]+$";

    /**
     * Maven version pattern.
     *
     * <p> Examples:
     * <p> 2.9.0 is valid
     * <p> 2.9.0-SNAPSHOT is valid
     * <p> Hoxton.SR12 is valid
     * <p> api-2.9.0 is invalid
     */
    public static final String VERSION_PATTERN = "^[^\\.-]+\\..+";

    /**
     * Illegal pattern message for {@link Exclude} action.
     */
    public static final String EXCLUDE_ILLEGAL_PATTERN_MESSAGE_FORMAT =
            "Illegal pattern: %s, only support maven coordinate and jar name, examples: %n"
                    + "- com.example:foo:1.0.0 %n"
                    + "- com.example:foo %n"
                    + "- foo-1.0.0.jar %n"
                    + "- foo-*.jar";
}
