package com.freemanan.cr.core.util;

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
}
