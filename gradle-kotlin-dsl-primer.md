# gradle kotlin dsl 入门

Gradle 的 Kotlin DSL 提供了传统 Groovy DSL 的替代语法，在受支持的 IDE 中具有增强的编辑体验，具有出色的内容辅助、重构、文档等。本章详细介绍了主要的 Kotlin DSL 构造以及如何使用它与 Gradle
API 交互。

## 先决条件

1. The embedded Kotlin compiler is known to work on Linux, macOS, Windows, Cygwin, FreeBSD and Solaris on x86-64
   architectures.
2. 熟悉Kotlin 语法和基本语言特性的知识
3. 使用 plugins {} 块声明 Gradle 插件可显着改善编辑体验，强烈推荐使用

## IDE 支持

IntelliJ IDEA 和 Android Studio 完全支持 Kotlin DSL。其他 IDE 尚未提供用于编辑 Kotlin DSL 文件的有用工具，但您仍然可以导入基于 Kotlin-DSL 的构建并照常使用它们。

此外，在编辑 Gradle 脚本时，IntelliJ IDEA 和 Android Studio 可能会产生多达 3 个 Gradle 守护进程——每种类型的脚本一个：构建脚本、设置文件和初始化脚本。配置时间较慢的构建可能会影响 IDE
响应能力，因此请查看性能部分以帮助解决此类问题。

我们建议您禁用自动构建导入，但启用脚本依赖项的自动重新加载。这样，您可以在编辑 Gradle 脚本时获得早期反馈，并控制整个构建设置何时与您的 IDE 同步。

## kotlin dsl 脚本

就像基于 Groovy 的等价物一样，Kotlin DSL 是在 Gradle 的 Java API 之上实现的。您可以在 Kotlin DSL 脚本中读取的所有内容都是由 Gradle 编译和执行的 Kotlin
代码。您在构建脚本中使用的许多对象、函数和属性来自 Gradle API 和应用插件的 API。

要激活 Kotlin DSL，只需为您的构建脚本使用 .gradle.kts 扩展名代替 .gradle。这也适用于设置文件——例如 settings.gradle.kts——和初始化脚本。

请注意，您可以将 Groovy DSL 构建脚本与 Kotlin DSL 构建脚本混合使用，即 Kotlin DSL 构建脚本可以应用一个 Groovy DSL，而多项目构建中的每个项目都可以使用其中一个。

### 隐式导入

所有 Kotlin DSL 构建脚本都有隐式导入，包括：

- 默认的 Gradle API 导入
- Kotlin DSL API，目前在 org.gradle.kotlin.dsl 和 org.gradle.kotlin.dsl.plugins.dsl 包中的所有类型。避免使用internal Kotlin DSL
  API，在插件和构建脚本中使用内部 Kotlin DSL API 有可能在 Gradle 或插件更改时破坏构建。 Kotlin DSL API 使用 org.gradle.kotlin.dsl 或
  org.gradle.kotlin.dsl.plugins.dsl 包（但不是这些包的子包）中的相应 API 文档中列出的类型扩展了 Gradle 公共 API。

## 类型安全模型访问器

Groovy DSL 允许您按名称引用构建模型的许多元素，即使它们是在运行时定义的。想想命名配置、命名源集等。For example, you can get hold of the implementation configuration
via configurations.implementation.

Kotlin DSL 用类型安全的模型访问器替换了这种动态解析，这些访问器与插件提供的模型元素一起工作。

### 了解何时可以使用类型安全模型访问器

Kotlin DSL 目前支持由插件提供的以下任何类型的类型安全模型访问器：

- Dependency 和 artifact 配置（`implementation` `runtimeOnly` 由java plugin 提供）
- Project 的扩展和约定 (`sourceSets`)
- tasks 和 configuration 容器中的元素
- project-extension 容器中的元素(例如，添加到 `sourceSets` 容器的 Java plugin 贡献的源集)
- 以上各项的扩展

> 只有主项目构建脚本和预编译项目脚本插件具有类型安全模型访问器。初始化脚本、设置脚本、脚本插件没有。这些限制将在未来的 Gradle 版本中删除。

可用的类型安全模型访问器集是在评估脚本主体之前计算的，紧接在 plugins {}
块之后。在此之后贡献的任何模型元素都不适用于类型安全的模型访问器。例如，这包括您可能在自己的构建脚本中定义的任何配置。但是，这种方法确实意味着您可以对由父项目应用的插件贡献的任何模型元素使用类型安全访问器。

> 您的 IDE 知道类型安全访问器，因此它会将它们包含在其建议中。这将发生在构建脚本的顶层——大多数插件扩展被添加到项目对象——以及配置扩展的块内。

请注意，容器元素（例如配置、任务和源集）的访问器利用了 Gradle 的配置避免 API。例如，在任务上，它们的类型为 TaskProvider<T> 并提供底层任务的惰性引用和惰性配置。以下是一些示例，说明了配置避免适用的情况：

```kotlin
tasks.test {
    // lazy configuration
}

// Lazy reference
val testProvider: TaskProvider<Test> = tasks.test

testProvider {
    // lazy configuration
}

// Eagerly realized Test task, defeat configuration avoidance if done out of a lazy context
val test: Test = tasks.test.get()
```

对于除tasks之外的所有其他容器，元素的访问器属于 NamedDomainObjectProvider<T> 类型并提供相同的行为。

### 了解当类型安全模型访问器不可用时该怎么做
考虑上面显示的示例构建脚本，它演示了类型安全访问器的使用。以下示例完全相同，只是使用 apply() 方法来应用插件。在这种情况下，构建脚本不能使用类型安全访问器，因为 apply() 调用发生在构建脚本的主体中。您必须改用其他技术，如下所示：

您也不能在 Kotlin 中实现的 Binary Gradle 插件中使用类型安全访问器。

如果找不到类型安全的访问器，请回退到对相应类型使用普通 API。为此，您需要知道配置的模型元素的名称和/或类型。现在，我们将通过详细查看上述脚本向您展示如何发现这些内容。

### Artifact 配置
以下示例演示了如何在没有类型访问器的情况下引用和配置工件配置：

