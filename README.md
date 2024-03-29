# Classpath Replacer

[![Build](https://img.shields.io/github/actions/workflow/status/DanielLiu1123/classpath-replacer/build.yml?branch=main)](https://github.com/DanielLiu1123/classpath-replacer/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.freemanan/classpath-replacer-core)](https://search.maven.org/artifact/com.freemanan/classpath-replacer-core)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

`Classpath Replacer` is essential for writing unit tests that require different classpath. 

If you need to test different classpath scenarios, this library is must-had.

## When you need it

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

`Classpath Replacer` can help you simulate this scenario in your unit tests.

## How to use

```groovy
// Gradle
testImplementation 'com.freemanan:classpath-replacer-junit5:2.1.2'
```

```xml
<!-- Maven -->
<dependency>
    <groupId>com.freemanan</groupId>
    <artifactId>classpath-replacer-junit5</artifactId>
    <version>2.1.2</version>
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
  version.

  ```java
  // add spring-boot:3.0.0 and its transitive dependencies to the classpath.
  @ClasspathReplacer(@Action(verb = ADD, value = "org.springframework.boot:spring-boot:3.0.0"))
  ```

- `EXCLUDE`：

  Exclude dependencies, value supports jar package name and maven coordinate.

  ```java
  // Exclude spring-boot-3.0.0.jar from the classpath, but not include its transitive dependencies.
  @ClasspathReplacer(@Action(verb = EXCLUDE, value = "spring-boot-3.0.0.jar"))
  
  // Same as above.
  @ClasspathReplacer(@Action(verb = EXCLUDE, value = "org.springframework.boot:spring-boot:3.0.0"))
  
  // Exclude all versions of spring-boot jars in the classpath. Using jar package name can't exclude transitive dependencies.
  @ClasspathReplacer(@Action(verb = EXCLUDE, value = "spring-boot-*.jar"))
  
  // If you want to exclude all versions of spring-boot jars, just omit the version
  @ClasspathReplacer(@Action(verb = EXCLUDE, value = "org.springframework.boot:spring-boot"))
  
  // Using maven coordinate doesn't exclude the transitive dependencies by default, you can set `recursiveExclude` to true to enable this feature.
  @ClasspathReplacer(recursiveExclude = true, value = {@Action(verb = EXCLUDE, value = "org.springframework.boot:spring-boot:3.0.0")})
  
  // exclude all versions of spring-boot jars and their transitive dependencies
  @ClasspathReplacer(recursiveExclude = true, value = {@Action(verb = EXCLUDE, value = "org.springframework.boot:spring-boot")})
  ```

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

## Limitation

**The static field will not be reusable!**

```java
public class StaticMethodTests {

  static AtomicInteger counter = new AtomicInteger(0);

  @Test
  @ClasspathReplacer({})
  void test1() {
    assertEquals(0, counter.getAndIncrement()); // pass
  }

  @Test
  @ClasspathReplacer({})
  void test2() {
    assertEquals(0, counter.getAndIncrement()); // pass
  }

}
```

Because each test method has a different classpath, it causes the test class to be reloaded, and static field/blocks will also be reinitialized.

If you want to use `@ClasspathReplacer` with `@SpringBootTest`, you need to consider the side effects that may come with restarting the Spring context.

If you want to use `@ClasspathReplacer` with [Testcontainers](https://www.testcontainers.org/), you need to consider the side effects that may come with restarting the container.

## Thanks

This project is inspired
by [spring-boot-test-support](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot-tools/spring-boot-test-support).
However, this library is only for internal use in `Spring Boot` and does not provide Maven coordinates for external use,
so `Classpath Replacer` was born.

## License

The MIT License.
