# Classpath Replacer

This is a framework for replacing classpath. It's very useful for testing.

## When you need

If you are a library developer, then you may need to test the behavior of your library under different classpath.

For example, you just write a [`JsonUtil`](examples/junit5/src/main/java/com/example/JsonUtil.java), you are only
responsible for providing the interface, and the specific implementation is up to the user to choose, such as
using `Jackson` or `Gson`.

Then you need to test your library in different scenarios:

- With `Jackson`, without `Gson`
- With `Gson`, without `Jackson`
- With both `Jackson` and `Gson`
- Different versions of behaviors (e.g. `Gson` 2.8.9 and 2.9.0)

But it is difficult to simulate this scenario in unit tests, because usually, unit tests are run under a fixed
classpath.

Classpath Replacer can help you simulate this scenario in your unit tests.

## How to use

`@ClasspathReplacer` is the core annotation of this framework. It can be used on the test class or test method.

You can define your classpath replacement rules in `@ClasspathReplacer`, which consists of `@Action`, each `@Action`
represents a classpath replacement rule.

`@Action` has two attributes:

- `verb`：The action to be performed on the classpath
    - `ADD`：add dependencies
    - `EXCLUDE`：exclude dependencies
    - `OVERRIDE`：override dependencies
- `value`：The value corresponding to verb

All `@Action` are executed in the order of definition, for example:

```java

@ClasspathReplacer({
        @Action(verb = ADD, value = "com.google.code.gson:gson:2.8.9"),
        @Action(verb = ADD, value = "com.google.code.gson:gson:2.9.0")
})
class SomeTest {
}
```

The above `@ClasspathReplacer` will add `gson:2.8.9` first, and then add `gson:2.9.0`. The final classpath will
be `gson:2.9.0`.

For the test scenario of the above `JsonUtil`, you can write the following tests:

```java
class JsonUtilTest {

    @Test
    void testNoJsonImplementationOnClasspath() {
        assertThrows(ExceptionInInitializerError.class, JsonUtil::instance);
    }

    @Test
    @ClasspathReplacer(@Action(verb = ADD, value = "com.google.code.gson:gson:2.10.1"))
    void testGsonOnClasspath() {
        assertTrue(JsonUtil.instance() instanceof Gson);
        assertEquals("{}", JsonUtil.toJson(new Object()));
    }

    @Test
    @ClasspathReplacer(@Action(verb = ADD, value = "com.fasterxml.jackson.core:jackson-databind:2.14.1"))
    void testJacksonOnClasspath() {
        assertTrue(JsonUtil.instance() instanceof Jackson);
    }

    @Test
    @ClasspathReplacer({
            @Action(verb = ADD, value = "com.fasterxml.jackson.core:jackson-databind:2.14.1"),
            @Action(verb = ADD, value = "com.google.code.gson:gson:2.10.1")
    })
    void useJacksonFirst_whenBothJacksonAndGsonOnClasspath() {
        assertTrue(JsonUtil.instance() instanceof Jackson);
    }

    @Test
    @ClasspathReplacer({@Action(verb = ADD, value = "com.google.code.gson:gson:2.8.9")})
    void gsonHasBugOn2_8_9_whenEmptyList() {
        assertThrows(InaccessibleObjectException.class, () -> {
            JsonUtil.toJson(Collections.emptyList());
        });
    }

    @Test
    @ClasspathReplacer({@Action(verb = ADD, value = "com.google.code.gson:gson:2.9.0")})
    void gsonWorksFineOn2_9_0_whenEmptyList() {
        assertDoesNotThrow(() -> {
            JsonUtil.toJson(Collections.emptyList());
        });
    }
}
```

## Thanks

This project is inspired
by [spring-boot-test-support](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot-tools/spring-boot-test-support).
However, this library is only for internal use in Spring Boot and does not provide Maven coordinates for external use,
so Classpath Replacer was born.

## License

The MIT License.
