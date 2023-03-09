# Classpath Replacer

[![Build](https://img.shields.io/github/actions/workflow/status/DanielLiu1123/classpath-replacer/build.yml?branch=main)](https://github.com/DanielLiu1123/classpath-replacer/actions)
[![CodeFactor](https://www.codefactor.io/repository/github/danielliu1123/classpath-replacer/badge)](https://www.codefactor.io/repository/github/danielliu1123/classpath-replacer)
[![Maven Central](https://img.shields.io/maven-central/v/com.freemanan/classpath-replacer-core)](https://search.maven.org/artifact/com.freemanan/classpath-replacer-core)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

This is a framework for replacing classpath.

When you need to write unit tests for different classpath scenarios, you will need it!

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

Gradle:

```groovy
testImplementation 'com.freemanan:classpath-replacer-jupiter:2.1.1'
```

Maven:

```xml

<dependency>
    <groupId>com.freemanan</groupId>
    <artifactId>classpath-replacer-jupiter</artifactId>
    <version>2.1.1</version>
    <scope>test</scope>
</dependency>
```

NOTE: from version `2.0.0`, `classpath-replacer` supports JDK 8, the previous versions only support JDK 17.

`@ClasspathReplacer` is the core annotation of this framework. It can be used on the test class or test method.

You can define your classpath replacement rules in `@ClasspathReplacer`, which consists of `@Action`, each `@Action`
represents a classpath replacement rule.

`@Action` has three verbs:

- `ADD`：

  Add dependencies, add dependencies if not exist, otherwise replace the existing dependency with the specified
  version. `@Action(verb = ADD, value = "org.springframework.boot:spring-boot:3.0.0")` will
  add `spring-boot:3.0.0` and **its transitive dependencies** to the classpath.

- `EXCLUDE`：

  Exclude dependencies, value supports jar package name and maven coordinate.

  `@Action(verb = EXCLUDE, value = "spring-boot-3.0.0.jar")`, this will exclude `spring-boot-3.0.0.jar` from the
  classpath, **but not include its transitive dependencies.** Support wildcard matching, such
  as `spring-boot-*.jar`, will exclude all versions of `spring-boot` jars in the classpath. Using jar package name
  **can't exclude transitive dependencies.**

  `@Action(verb = EXCLUDE, value = "org.springframework.boot:spring-boot:3.0.0")`, this is same as above. If you want to
  exclude all versions of `spring-boot` jars,
  use `@Action(verb = EXCLUDE, value = "org.springframework.boot:spring-boot")`, just omit the version.

  Using maven coordinate doesn't exclude the transitive dependencies by default, you can set `recursiveExclude` to
  true to enable this feature.
  `@ClasspathReplacer(recursiveExclude = true, value = {@Action(verb = EXCLUDE, value = "org.springframework.boot:spring-boot:3.0.0")})`
  will exclude `spring-boot:3.0.0` and its transitive dependencies. You can omit the
  version, `@ClasspathReplacer(recursiveExclude = true, value = {@Action(verb = EXCLUDE, value = "org.springframework.boot:spring-boot")})`
  will exclude all versions of `spring-boot` jars and their transitive dependencies.

- `OVERRIDE`：

  **`Override` has the same behavior as `ADD`**, separate `ADD` and `OVERRIDE` just for clearer semantic expression.

All the `@Action` are executed in the order of definition, for example:

```java

@ClasspathReplacer({
        @Action(verb = ADD, value = "com.google.code.gson:gson:2.8.9"),
        @Action(verb = EXCLUDE, value = "com.google.code.gson:gson:2.8.9"),
        @Action(verb = ADD, value = "com.google.code.gson:gson:2.9.0")
})
class SomeTest {
}
```

The above `@ClasspathReplacer` will add `gson:2.8.9` first, and exclude `gson:2.8.9`, then add `gson:2.9.0`. The final
classpath will be `gson:2.9.0`.

Proxy repositories and private repositories can be configured in `@ClasspathReplacer`, such as:

```java

@ClasspathReplacer(
        repositories = {
                @Repository("https://maven.aliyun.com/repository/public/"),
                @Repository(value = "https://maven.youcompany.com/repository/release/", username = "admin", password = "${MAVEN_PASSWORD}")
        },
        value = {
                @Action(verb = ADD, value = "com.yourcompany:your-library:1.0.0")
        })
class SomeTest {
}
```

`username` and `password` support `${}` variable replacement, it will be parsed from system env or system properties, system env has higher priority.

For the test scenarios of the above `JsonUtil`, you can write the following tests:

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
}
```

## Thanks

This project is inspired
by [spring-boot-test-support](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot-tools/spring-boot-test-support).
However, this library is only for internal use in Spring Boot and does not provide Maven coordinates for external use,
so Classpath Replacer was born.

## License

The MIT License.
