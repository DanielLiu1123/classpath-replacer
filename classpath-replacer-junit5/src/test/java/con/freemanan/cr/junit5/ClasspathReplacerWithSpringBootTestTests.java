package con.freemanan.cr.junit5;

import static com.freemanan.cr.core.anno.Verb.ADD;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Known defect, when using {@link ClasspathReplacer} with {@link SpringBootTest},
 * the test methods <strong>can not</strong> reuse the {@link ApplicationContext} instance,
 * a new {@link ApplicationContext} will be started for each test method !
 *
 * <p> {@link SpringBootTest} starts a {@link ApplicationContext} on very early stage,
 * so this test will start ApplicationContext 3 times because existing 2 test methods.
 *
 * @author Freeman
 * @see ClasspathReplacerWithSpringBootTests
 */
@ClasspathReplacer(@Action(verb = ADD, value = "org.springframework.boot:spring-boot-starter-web:2.7.5"))
@SpringBootTest(
        classes = ClasspathReplacerWithSpringBootTestTests.Cfg.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ClasspathReplacerWithSpringBootTestTests {

    @Autowired
    ConfigurableApplicationContext ctx;

    @Test
    void hasFoo_whenHasWebStarter() {
        assertDoesNotThrow(() -> ctx.getBean(Foo.class));
    }

    @Test
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
