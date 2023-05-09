## Introduction

`Classpath Replacer` is essential for writing unit tests that require different classpath. 

If you need to test different classpath scenarios, this library is very useful.

## Installation

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

## Features

### Add

### Exclude

### Override

### Customized Repository

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
