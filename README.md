# learn-kotlin

学习 kotlin 在 Spring 项目中的应用，

> Building web applications with Spring Boot and Kotlin This tutorial shows you how to build efficiently a sample blog application by combining the power of Spring Boot and Kotlin. If you are starting with Kotlin, you can learn the language by reading the reference documentation, following the online Kotlin Koans tutorial or just using Spring Framework reference documentation which now provides code samples in Kotlin. Spring Kotlin support is documented in the Spring Framework and Spring Boot reference documentation. If you need help, search or ask questions with the spring and kotlin tags on StackOverflow or come discuss in the #spring channel of Kotlin Slack.

以下内容为个人翻译和总结官方的相关文档。[查看原文 Spring Boot Features](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-kotlin)

## 30. Kotlin support 对kotlin的支持

Kotlin是一种针对 JVM（还有其他平台JS，Native）的静态类型语言，允许编写简洁优雅的代码，同时对现有通过 Java 编写的库提供良好的互操作性。

Spring Boot 利用其他多个Spring 项目（例如：Spring Framework、Spring Data、Reactor）对Kotlin的提供了支持。

### 30.1 Requirements 基本要求

Spring Boot 支持 Kotlin 1.3.x 。想要使用kotlin，必须在 classpath 中引入 `org.jetbrains.kotlin:kotlin-stdlib`
和 `org.jetbrains.kotlin:kotlin-reflect`。`kotlin-stdlib`标准库有多个变种版本，`kotlin-stdlib-jdk7` 和 `kotlin-stdlib-jdk8` 均可使用。

由于kotlin 的类class 默认是 final 的， 你可能想要配置 kotlin-spring plugin插件来自动开放 Spring-annotated 类classes以便这些类能被代理。
> 您需要使用`kotlin-spring` 插件自动将`@Configuration`类和其他一些`@Service`或`@Repository`设置为`open`，因为由于`CGLIB代理`的使用，它们在Spring中无法最终确定（默认情况下，Kotlin中的类和方法是`final`，默认是没有`open`修饰符的）。使用`JDK动态代理`的Bean不需要`open`修饰符。

在kotlin中，`Jackson’s Kotlin module` 模块需要序列化/反序列化 JSON 数据。在classpath上找到时，这个模块被自动注册。如果Jackson 和 kotlin 的包被提供，但是 Jackson
Kotlin module 的包没有提供，则会在后台打印出警告信息warning。

> 如果你使用的是start.spring.io提供的项目，以上提到的包依赖和插件均默认提供。

### 30.2 Null-safety 空安全

空安全是kotlin 的一个关键特性。这样就可以在编译期处理 null空值问题，而不是将问题推迟到运行期解决并，也不会遇到空指针问题。这样有助于消除错误来源，而不需要使用大量的 Optional 进行包装。

通过 kotlin提供了对 JSR 305 annotations 的支持以及空类型的注解，为Spring kotlin 相关API的空安全的支持。

JSR 305 检查可以通过 `-Xjsr305` 编译标志参数来配置开启，包含以下选项：`-Xjsr305={strict|warn|ignore}`。默认行为是 `-Xjsr305=warn`
。如果配置 `-Xjsr305=strict`，那么会从Spring API中推断出来，在kotlin类型中， 必须考虑空安全性问题。但是在使用时应该考虑到Spring API 的可空性声明可能会在小版本之间发展，
并在将来可能会添加更多的检查。

> 目前泛型类型参数、varargs、数组元素不支持可空性。See SPR-15942 for up-to-date information. Also be aware that Spring Boot’s own API is not yet annotated.

### 30.3 Kotlin API

#### 30.3.1 runApplication 启动应用

使用`runApplication<MyApplication>(*args)`Spring Boot 提供惯用的方式运行应用。

```kotlin
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MyApplication

fun main(args: Array<String>) {
    runApplication<MyApplication>(*args)
}
```

也可以自定义应用的启动banner展示效果。

```kotlin
runApplication<MyApplication>(*args) {
    setBannerMode(OFF)
}
```

#### 30.3.2 Extensions 扩展

kotlin extensions 提供了扩展具有附加功能的现有类的能力。Spring Boot Kotlin API利用这些 extensions 为现有的API添加了新的Kotlin特定的便利功能。

提供了 TestRestTemplate 扩展，类似于 Spring Framework 中针对 RestOperations 的 Spring Framework 提供的扩展。

### 30.4 Dependency management 依赖管理

为了避免在类路径中混合使用不同版本的Kotlin依赖项，Spring Boot会导入Kotlin BOM。

使用Maven，可以通过kotlin.version属性自定义Kotlin版本，并且为kotlin-maven-plugin提供了插件管理。使用Gradle，Spring
Boot插件会自动将kotlin.version与Kotlin插件的版本对齐。

Spring Boot还通过导入Kotlin Coroutines BOM管理Coroutines依赖项的版本。可以通过kotlin-coroutines.version属性自定义版本。

> `org.jetbrains.kotlinx:kotlinx-coroutines-reactor` 依赖默认是被提供的，如果你启动了一个 start.spring.io 的 Kotlin 项目并配置了一个 reactive 依赖。

### 30.5 @ConfigurationProperties 配置

`@ConfigurationProperties` 与 `@ConstructorBinding` 结合使用时，`@ConfigurationProperties` 支持具有不变 `val`属性的类上使用，如以下示例所示：

```kotlin
@ConstructorBinding
@ConfigurationProperties("example.kotlin")
data class KotlinExampleProperties(
    val name: String,
    val description: String,
    val myService: MyService
) {

    data class MyService(
        val apiToken: String,
        val uri: URI
    )
}
```

> 如果想要生成自己的metadata 需要使用 annotation processor，为kapt配置 `spring-boot-configuration-processor` 依赖项。 请注意，由于kapt提供的模型的限制，某些功能（例如检测默认值或不推荐使用的项目）无法正常工作。

### 30.6 Testing 测试
虽然可以使用JUnit 4测试Kotlin代码，但默认情况下建议使用JUnit 5。JUnit 5可以一次实例化一个测试类，然后将其重新用于该类的所有测试。这样就可以在非静态方法上使用`@BeforeAll`和`@AfterAll`注解，这非常适合Kotlin。

要mock Kotlin类，建议使用 [MockK](https://mockk.io/) 。如果您需要`Mockk`等效于`Mockito`特定的`@MockBean`和`@SpyBean`注解，则可以使用 [SpringMockK](https://github.com/Ninja-Squad/springmockk) ，它提供类似的`@MockkBean`和`@SpykBean`批注。

### 30.7 Resources 相关资源

#### 30.7.1 Further reading 延伸阅读

- [Kotlin language reference](https://kotlinlang.org/docs/reference/)
- [Kotlin Slack](https://kotlinlang.slack.com/) (with a dedicated #spring channel)
- [Stackoverflow with `spring` and `kotlin` tags](https://stackoverflow.com/questions/tagged/spring+kotlin)
- [Try Kotlin in your browser](https://try.kotlinlang.org/)
- [Kotlin blog](https://blog.jetbrains.com/kotlin/)
- [Awesome Kotlin](https://kotlin.link/)
- [Tutorial: building web applications with Spring Boot and Kotlin](https://spring.io/guides/tutorials/spring-boot-kotlin/)
- [Developing Spring Boot applications with Kotlin](https://spring.io/blog/2016/02/15/developing-spring-boot-applications-with-kotlin)
- [A Geospatial Messenger with Kotlin, Spring Boot and PostgreSQL](https://spring.io/blog/2016/03/20/a-geospatial-messenger-with-kotlin-spring-boot-and-postgresql)
- [Introducing Kotlin support in Spring Framework 5.0](https://spring.io/blog/2017/01/04/introducing-kotlin-support-in-spring-framework-5-0)
- [Spring Framework 5 Kotlin APIs, the functional way](https://spring.io/blog/2017/08/01/spring-framework-5-kotlin-apis-the-functional-way)

#### 30.7.2 Examples 事例

- [spring-boot-kotlin-demo](https://github.com/sdeleuze/spring-boot-kotlin-demo): regular Spring Boot + Spring Data JPA project
- [mixit](https://github.com/mixitconf/mixit): Spring Boot 2 + WebFlux + Reactive Spring Data MongoDB
- [spring-kotlin-fullstack](https://github.com/sdeleuze/spring-kotlin-fullstack): WebFlux Kotlin fullstack example with Kotlin2js for frontend instead of JavaScript or TypeScript
- [spring-petclinic-kotlin](https://github.com/spring-petclinic/spring-petclinic-kotlin): Kotlin version of the Spring PetClinic Sample Application
- [spring-kotlin-deepdive](https://github.com/sdeleuze/spring-kotlin-deepdive): a step by step migration for Boot 1.0 + Java to Boot 2.0 + Kotlin
- [spring-boot-coroutines-demo](https://github.com/sdeleuze/spring-boot-coroutines-demo): Coroutines sample project



