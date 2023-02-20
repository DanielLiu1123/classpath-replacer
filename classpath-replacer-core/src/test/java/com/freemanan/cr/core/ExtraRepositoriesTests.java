package com.freemanan.cr.core;

import static com.freemanan.cr.core.anno.Verb.ADD;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import com.freemanan.cr.core.anno.Repository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Freeman
 */
public class ExtraRepositoriesTests {
    @Test
    @Disabled("This test is only for manual testing. Please ignore it.")
    @ClasspathReplacer(
            value = {
                @Action(verb = ADD, value = "com.example:example:1.1-SNAPSHOT"),
            },
            repositories = {
                @Repository(
                        value = "https://packages.aliyun.com/maven/repository/2151940-snapshot-GcO0aN",
                        username = "${MAVEN_USER}",
                        password = "${MAVEN_PASSWORD}"),
            })
    void testPrivateRepository() {}
}
