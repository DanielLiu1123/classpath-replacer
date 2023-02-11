package com.freemanan.cr.core.util;

import static com.freemanan.cr.core.util.Const.JAR_FILE_NAME_PATTERN;
import static com.freemanan.cr.core.util.Const.MAVEN_COORDINATE_PATTERN;
import static com.freemanan.cr.core.util.Const.MAVEN_COORDINATE_WITH_VERSION_PATTERN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * {@link Const} tester.
 */
class ConstTest {

    @Test
    void testRegex() {
        assertFalse("com.google.code.gson:".matches(MAVEN_COORDINATE_PATTERN));
        assertTrue("com.google.code.gson:gson".matches(MAVEN_COORDINATE_PATTERN));
        assertFalse("com.google.code.gson:gson:".matches(MAVEN_COORDINATE_PATTERN));
        assertTrue("com.google.code.gson:gson:2.9.0".matches(MAVEN_COORDINATE_PATTERN));

        assertFalse("com.google.code.gson:".matches(MAVEN_COORDINATE_WITH_VERSION_PATTERN));
        assertFalse("com.google.code.gson:gson".matches(MAVEN_COORDINATE_WITH_VERSION_PATTERN));
        assertFalse("com.google.code.gson:gson:".matches(MAVEN_COORDINATE_WITH_VERSION_PATTERN));
        assertTrue("com.google.code.gson:gson:2.9.0".matches(MAVEN_COORDINATE_WITH_VERSION_PATTERN));

        assertFalse(".jar".matches(JAR_FILE_NAME_PATTERN));
        assertFalse("gson-2.9.0".matches(JAR_FILE_NAME_PATTERN));
        assertTrue("gson-2.9.0.jar".matches(JAR_FILE_NAME_PATTERN));
        assertTrue("gson-2.9.0-javadoc.jar".matches(JAR_FILE_NAME_PATTERN));
    }
}
