package com.example;

import static com.freemanan.cr.core.anno.Action.Verb.ADD;

import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author Freeman
 * @since 2023/2/12
 */
@ClasspathReplacer({
    @Action(verb = ADD, value = "com.fasterxml.jackson.core:jackson-databind:2.14.1"),
})
public class CacheTests {

    @Test
    void test01() {
        System.out.println(JsonUtil.toJson(Map.of()));
    }

    @Test
    void test02() {
        System.out.println(JsonUtil.toJson(Map.of()));
    }
}
