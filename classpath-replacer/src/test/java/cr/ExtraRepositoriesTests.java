package cr;

import static cr.Verb.ADD;

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
