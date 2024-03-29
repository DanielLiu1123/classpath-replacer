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

`@ClasspathReplacer` is the core annotation of this library, which can be used to replace the classpath of the current test class/method.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ClasspathReplacer {
    Action[] value();
    Repository[] repositories() default {};
    boolean recursiveExclude() default false;
}
```

`@ClasspathReplacer` is mainly composed of multiple `Action`s, there are 3 types of `Action`s: `Add`, `Exclude`, `Override`.

### Add

Add action is used to add a dependency to the classpath, add this is also the default action.

```java
class JacksonTest {
    
    @Test
    void noJacksonOnClasspath() {
        Assertions.assertThatCode(() -> Class.forName("com.fasterxml.jackson.databind.ObjectMapper"))
                .isInstanceOf(ClassNotFoundException.class);
    }
    
    @Test
    @ClasspathReplacer({
            @Action(verb = Action.ADD, value = "com.fasterxml.jackson.core:jackson-databind:2.14.2")
    })
    void jacksonOnClasspath() {
        Assertions.assertThatCode(() -> Class.forName("com.fasterxml.jackson.databind.ObjectMapper"))
                .doesNotThrowAnyException();
    }
}
```

`@ClasspathReplacer` can be used on both class and method, use the method annotation first.

```java
@ClasspathReplacer({
        @Action(verb = Action.ADD, value = "org.springframework.boot:spring-boot-starter:2.6.0")
})
class SpringBootTest {
    
    @Test
    @ClasspathReplacer({
            @Action(verb = Action.ADD, value = "org.springframework.boot:spring-boot-starter:2.7.0")
    })
    void testSpringBootVersion() throws Exception {
        Class<?> clazz = Class.forName("org.springframework.boot.SpringBootVersion");
        String version = clazz.getMethod("getVersion").invoke(null);
        Assertion.assertThat(version).isEqualTo("2.7.0");
    }
}
```

If there already exists the dependency on the classpath, it will be overridden by the new version.

```java
class SpringBootTest {
    
    @Test
    @ClasspathReplacer({
            @Action(verb = Action.ADD, value = "org.springframework.boot:spring-boot-starter:2.7.0")
    })
    void testSpringBootVersion() {
        // If already exists Spring Boot 2.6.0 on classpath, it will be overridden by 2.7.0
        String version = SpringBootVersion.getVersion();
        Assertion.assertThat(version).isEqualTo("2.7.0");
    }
}
```

### Exclude

`Exclude` action is used to exclude a dependency from the classpath.

```java
class SpringBootTest {
    
    @Test
    @ClasspathReplacer({
            @Action(verb = Action.EXCLUDE, value = "org.springframework.boot:spring-boot-starter:2.7.0")
    })
    void testExclude() {
        Assertions.assertThatCode(() -> Class.forName("org.springframework.boot.SpringBootVersion"))
                .isInstanceOf(ClassNotFoundException.class);
    }
}
```

`Exclude` action only remove the current jar from the classpath and not remove the transitive dependencies!

You can use `recursiveExclude` to remove the transitive dependencies.

```java
@ClasspathReplacer(
        value = {
            @Action(verb = Action.EXCLUDE, value = "org.springframework.boot:spring-boot-starter:2.7.0")
        },
        recursiveExclude = true
)
```

You can omit the version of the dependency, and it will remove all versions of the dependency.

```java
@ClasspathReplacer({
        @Action(verb = Action.EXCLUDE, value = "org.springframework.boot:spring-boot-starter")
})
```

> It will remove all versions of the dependency and transitive dependencies if `recursiveExclude` is `true`.

You can also use a jar file name pattern to exclude the dependency.

```java
@ClasspathReplacer({
        @Action(verb = Action.EXCLUDE, value = "spring-boot-starter-*.jar")
})
```

This will exclude all jar files that match the pattern.

> `recursiveExclude` is not supported for a jar file name pattern, because the transitive dependencies cannot be determined from the jar file name.

### Override

**`Override` action has the same behavior as `Add` action!** Separating `ADD` and `OVERRIDE` for clearer semantic expression.

### Customized Repository

Customized repository is supported.

```java
@ClasspathReplacer(
        value = {
            @Action(verb = ADD, value = "com.example:example:1.0-SNAPSHOT")
        },
        repositories = {
            @Repository(
                    value = "https://maven.youcompany.com/maven/repository/xxx",
                    username = "${MAVEN_USER}",
                    password = "${MAVEN_PASSWORD}")
        }
)
```

You can use `${}` to reference environment variables or system properties, using environment variables first.

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

Because each test method has a different classpath, it causes the test class to be reloaded, and static field/blocks will also be reinitialized for each test method invocation.

If you want to use `@ClasspathReplacer` with `@SpringBootTest`, you need to consider the side effects that may come with restarting the Spring context.

If you want to use `@ClasspathReplacer` with [Testcontainers](https://www.testcontainers.org/), you need to consider the side effects that may come with restarting the container.

## Thanks

This project is inspired
by [spring-boot-test-support](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot-tools/spring-boot-test-support).
However, this library is only for internal use in `Spring Boot` and does not provide Maven coordinates for external use,
so `Classpath Replacer` was born.
