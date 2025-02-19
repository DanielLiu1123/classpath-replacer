# Classpath Replacer

[![Build](https://img.shields.io/github/actions/workflow/status/DanielLiu1123/classpath-replacer/build.yml?branch=main)](https://github.com/DanielLiu1123/classpath-replacer/actions)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.danielliu1123/classpath-replacer)](https://search.maven.org/artifact/io.github.danielliu1123/classpath-replacer)
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

But it is challenging to simulate this scenario in unit tests, because usually, unit tests are run under a fixed
classpath.

`Classpath Replacer` can help you simulate this scenario in your unit tests.

## How to use

```groovy
// Gradle
testImplementation "io.github.danielliu1123:classpath-replacer:<latest>"
```

```xml
<!-- Maven -->
<dependency>
    <groupId>io.github.danielliu1123</groupId>
    <artifactId>classpath-replacer</artifactId>
    <version>latest</version>
    <scope>test</scope>
</dependency>
```

`@Classpath` is the core annotation of this framework. It can be used on the test class or test method, method-level annotation will override the class-level annotation.

Examples:

```java
// add spring-boot:3.0.0 and its transitive dependencies to the classpath.
@Classpath(add = "org.springframework.boot:spring-boot:3.0.0")

// Exclude spring-boot-3.0.0.jar from the classpath, but not include its transitive dependencies.
// value uses the jar package name.
@Classpath(exclude = "spring-boot-3.0.0.jar")

// Exclude spring-boot-3.0.0.jar from the classpath, but not include its transitive dependencies.
// value uses the maven coordinate.
@Classpath(exclude = "org.springframework.boot:spring-boot:3.0.0")

// Exclude all versions of spring-boot jars in the classpath. 
// Using jar package name can't exclude transitive dependencies.
@Classpath(exclude = "spring-boot-*.jar")

// If you want to exclude all versions of spring-boot jars, just omit the version
@Classpath(exclude = "org.springframework.boot:spring-boot")

// Using maven coordinate doesn't exclude the transitive dependencies by default, you can set `excludeTransitive` to true.
@Classpath(exclude = "org.springframework.boot:spring-boot:3.0.0", excludeTransitive = true)

// exclude all versions of spring-boot jars and their transitive dependencies
@Classpath(exclude = "org.springframework.boot:spring-boot", excludeTransitive = true)
```

For the test scenarios of the above `JsonUtil`, you can write the following tests:

```java
class JsonUtilTest {

  @Test
  void testNoJsonImplementationOnClasspath() {
    assertThrows(ExceptionInInitializerError.class, JsonUtil::instance);
  }

  @Test
  @Classpath(add = "com.google.code.gson:gson:2.10.1")
  void testGsonOnClasspath() {
    assertTrue(JsonUtil.instance() instanceof Gson);
    assertEquals("{}", JsonUtil.toJson(new Object()));
  }

  @Test
  @Classpath(add = "com.fasterxml.jackson.core:jackson-databind:2.14.1")
  void testJacksonOnClasspath() {
    assertTrue(JsonUtil.instance() instanceof Jackson);
  }

  @Test
  @Classpath(add = {"com.fasterxml.jackson.core:jackson-databind:2.14.1", "com.google.code.gson:gson:2.10.1"})
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
  @Classpath
  void test1() {
    assertEquals(0, counter.getAndIncrement()); // pass
  }

  @Test
  @Classpath
  void test2() {
    assertEquals(0, counter.getAndIncrement()); // pass
  }

}
```

Because each test method has a different classpath, it causes the test class to be reloaded, and static field/blocks will also be reinitialized.

If you want to use `@Classpath` with `@SpringBootTest`, you need to consider the side effects that may come with restarting the Spring context.

If you want to use `@Classpath` with [Testcontainers](https://www.testcontainers.org/), you need to consider the side effects that may come with restarting the container.

## Thanks

This project is inspired
by [spring-boot-test-support](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot-tools/spring-boot-test-support).
However, this library is only for internal use in `Spring Boot` and does not provide Maven coordinates for external use,
so `Classpath Replacer` was born.

## License

The MIT License.
