package con.freemanan.cr.junit5;

import static com.freemanan.cr.core.anno.Verb.ADD;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * {@link ClasspathReplacer} will skip the test method invocation,
 * then re-initiate the test class, so static fields and static blocks will be re-executed,
 * in the same way, the @BeforeAll and @AfterAll methods will be re-executed, all the Extensions will be re-executed,
 * like {@link SpringBootTest}, {@link org.testcontainers.junit.jupiter.Testcontainers}.
 *
 * <p> This test will start ApplicationContext twice because existing 2 test methods.
 *
 * @author Freeman
 * @see ClasspathReplacerWithSpringBootTestTests
 */
@ClasspathReplacer(@Action(verb = ADD, value = "org.springframework.boot:spring-boot-starter-web:2.7.5"))
class ClasspathReplacerWithSpringBootTests {

    static ConfigurableApplicationContext ctx;

    @BeforeAll
    static void setup() {
        ctx = new SpringApplicationBuilder(Cfg.class)
                .web(WebApplicationType.NONE)
                .run();
    }

    @AfterAll
    static void reset() {
        ctx.close();
    }

    @Test
    void hasFoo_whenHasWebStarter() {
        assertDoesNotThrow(() -> ctx.getBean(Foo.class));
    }

    @Test
    // just override the @ClasspathReplacer on class level
    @ClasspathReplacer({})
    void noFoo_whenNoWebStarter() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> ctx.getBean(Foo.class));
    }

    @Configuration(proxyBeanMethods = false)
    @Import(Foo.class)
    static class Cfg {}

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.web.client.RestTemplate")
    static class Foo {}
}
