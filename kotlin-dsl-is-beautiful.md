# 学习 kotlin dsl 在Spring中的应用

- DSL (domain specific language)领域专用语言：解决某一特定问题的计算机语言。
- DSL 具备独特的代码结构、一致的代码风格。"更有表现力、想象力，也更加优雅"。
- kotlin dsl 的实现原理利用了kotlin的以下语法特性。

## 扩展函数（扩展属性）
Kotlin 实现原理是： 提供静态工具类，将接收对象(此例为 String )做为参数传递进来,以下为该扩展函数编译成 Java 的代码
```
char c = StringUtilKt.lastChar("Java");
```

```kotlin
package strings

fun String.lastChar(): Char = this.get(this.length - 1)
```
调用 `>>> println("Kotlin".lastChar())` 即可。

## lambda
1. lambda表达式
lambda表达式总是用一对`{}`包装起来，可以作为值传递给高阶函数。
```kotlin
{ x: Int, y: Int -> x + y}
```

2. 高阶函数
高阶函数就是`以另一个函数作为参数或返回值`的函数。lambda表达式作为高阶函数的参数（形参）时，需要例如：`(Int, String) -> Unit`。
   ```kotlin
    // printSum 为高阶函数，定义了 lambda 形参
    fun printSum(sum:(Int,Int)->Int){
    val result = sum(1, 2)
    println(result)
    }
    
    // 以下 lambda 为实参，传递给高阶函数 printSum
    val sum = {x:Int,y:Int->x+y}
    printSum(sum)
    ```
   
3. lambda规约
如果lambda表达式作为函数的最后一个实参，则可以放在`()`外面，只有一个参数时，可以省略`()`。
   ```kotlin
   person.maxBy({ p:Person -> p.age })
   
   // 可以写成
   person.maxBy(){
       p:Person -> p.age
   }
   
   // 更简洁的风格：
   person.maxBy{
       p:Person -> p.age
   }
   ```

   ```kotlin
   verticalLayout {
      val name = editText()
      button("Say Hello") {
          onClick { toast("Hello, ${name.text}!") }
      }
   }
   
   fun verticalLayout( () -> Unit ){
       
   }
   
   fun button( text:String,() -> Unit ){
       
   }
   ```


4. 带接受者receiver的lambda
lambda作为形参时，可以携带接收者。带接收者的lambda丰富了函数声明的信息。例如： `String.(Int, Int) -> Unit`
   ```kotlin
   // 声明接收者
   fun kotlinDSL(block:StringBuilder.()->Unit){
       block(StringBuilder("Kotlin"))
   }
   
   // 调用高阶函数
   kotlinDSL {
       // 这个 lambda 的接收者类型为StringBuilder
       append(" DSL")
       println(this)
   }
   
   // >>> 输出 Kotlin DSL
   ```

## 中缀调用
infix 修饰符代表该函数支持中缀调用。
```kotlin
object 前
infix fun Int.天(ago:前) = LocalDate.now() - Period.ofDays(this)
// val yesterday = 1 天 前

object start
infix fun String.should(start:start):String = ""
infix fun String.with(str:String):String = ""
// "kotlin" should start with "kot"
// 等价于 "kotlin".should(start).with("kot")
```

## invoke约定
Kotlin 提供了 invoke 约定，可以让对象向函数一样直接调用。invoke 约定让对象调用函数的语法结构更加简洁。
```kotlin
class Person(val name:String){
    operator fun invoke(){
        println("my name is $name")
    }
}

//>>>val person = Person("geniusmart")
//>>> person()
//my name is geniusmart
```

----
延伸阅读：介绍kotlin 函数式 bean 声明 DSL 的实现

Spring Framework 5.0引入了一种新的方式来使用lambda注册bean，以作为`@Configuration`和`@Bean`的XML或JavaConfig的替代方法。简而言之，它使向具有`FactoryBean`的`Supplier lambda`注册bean成为可能。

```
GenericApplicationContext context = new GenericApplicationContext();
context.registerBean(Foo.class);
context.registerBean(Bar.class, () -> new 
	Bar(context.getBean(Foo.class))
);
```
等同的kotlin实现，则是
```kotlin
beans {
    bean<Foo>()
    bean { Bar(ref()) }
}
```

> Introduce Kotlin functional bean definition DSL
> 
> As a follow-up of the ApplicationContext Kotlin extensions, close to the Kotlin functional WebFlux DSL and partially inspired of the Groovy/Scala bean configuration DSL, this commit introduces a lightweight Kotlin DSL for functional bean declaration.
>
> 作为ApplicationContext Kotlin扩展的后续， Kotlin 函数式 WebFlux DSL，部分受启发于 Groovy / Scala bean配置DSL，此提交引入了一个 轻量级的Kotlin DSL，用于函数式 bean 声明。
> 
> It allows declaring beans as following:
> 
> ```kotlin
> beans {
> bean<Foo>()
> profile("bar") {
> bean<Bar>("bar", scope = Scope.PROTOTYPE)
> }
> environment({ it.activeProfiles.contains("baz") }) {
> bean { Baz(it.ref()) }
> bean { Baz(it.ref("bar")) }
> }
> }
> ```
> Advantages compared to Regular ApplicationContext API are:
> - No exposure of low-level ApplicationContext API 不暴露低级ApplicationContext API
> - Focused DSL easier to read, but also easier to write with a fewer entries in the auto-complete 聚焦于dsl, 更易于阅读，通过自动补全更少的条目，也更容易写入
> - Declarative syntax instead of functions with verbs like registerBeans while still allowing programmatic registration of beans if needed 声明性语法而不是带有诸如registerBeans之类的动词的函数 同时仍允许以编程方式注册Bean（如果需要）
> - Such DSL is idiomatic in Kotlin 这种DSL在Kotlin中是惯用的
> - No need to have an ApplicationContext instance to write how you register your beans since beans { } DSL is conceptually a Consumer<GenericApplicationContext> 无需使用ApplicationContext实例，来编写如何注册bean，因为 `beans{}` DSL语法，在概念上是一个消费者`Consumer<GenericApplicationContext>`
> 
> This DSL effectively replaces ApplicationContext Kotlin extensions as the recommended way to register beans in a functional way with Kotlin.
> 
> 在使用kotlin时，该DSL有效地替代了ApplicationContext Kotlin扩展，使用函数式 bean 声明是推荐做法。
> 
> Issue: SPR-15755


以下是一段Spring 使用kotlin 函数式的 bean 声明的源代码
```kotlin
//spring-context/src/main/kotlin/org/springframework/context/support/BeanDefinitionDsl.kt
package org.springframework.context.support

import org.springframework.beans.factory.config.BeanDefinitionCustomizer
import org.springframework.context.ApplicationContext
import org.springframework.core.env.ConfigurableEnvironment
import java.util.function.Supplier

/**
 * Class implementing functional bean definition Kotlin DSL.
 *
 * @author Sebastien Deleuze
 * @since 5.0
 */
open class BeanDefinitionDsl(val condition: (ConfigurableEnvironment) -> Boolean = { true }) : (GenericApplicationContext) -> Unit {

   protected val registrations = arrayListOf<(GenericApplicationContext) -> Unit>()

   protected val children = arrayListOf<BeanDefinitionDsl>()

   enum class Scope {
      SINGLETON,
      PROTOTYPE
   }

   class BeanDefinitionContext(val context: ApplicationContext) {

      inline fun <reified T : Any> ref(name: String? = null) : T = when (name) {
         null -> context.getBean(T::class.java)
         else -> context.getBean(name, T::class.java)
      }
   }

   /**
    * Declare a bean definition from the given bean class which can be inferred when possible.
    *
    * @See GenericApplicationContext.registerBean
    */
   inline fun <reified T : Any> bean(name: String? = null,
                                     scope: Scope? = null,
                                     isLazyInit: Boolean? = null,
                                     isPrimary: Boolean? = null,
                                     isAutowireCandidate: Boolean? = null) {

      registrations.add {
         val customizer = BeanDefinitionCustomizer { bd ->
            scope?.let { bd.scope = scope.name.toLowerCase() }
            isLazyInit?.let { bd.isLazyInit = isLazyInit }
            isPrimary?.let { bd.isPrimary = isPrimary }
            isAutowireCandidate?.let { bd.isAutowireCandidate = isAutowireCandidate }
         }

         when (name) {
            null -> it.registerBean(T::class.java, customizer)
            else -> it.registerBean(name, T::class.java, customizer)
         }
      }
   }

   /**
    * Declare a bean definition using the given supplier for obtaining a new instance.
    *
    * @See GenericApplicationContext.registerBean
    */
   inline fun <reified T : Any> bean(name: String? = null,
                                     scope: Scope? = null,
                                     isLazyInit: Boolean? = null,
                                     isPrimary: Boolean? = null,
                                     isAutowireCandidate: Boolean? = null,
                                     crossinline function: (BeanDefinitionContext) -> T) {

      val customizer = BeanDefinitionCustomizer { bd ->
         scope?.let { bd.scope = scope.name.toLowerCase() }
         isLazyInit?.let { bd.isLazyInit = isLazyInit }
         isPrimary?.let { bd.isPrimary = isPrimary }
         isAutowireCandidate?.let { bd.isAutowireCandidate = isAutowireCandidate }
      }

      registrations.add {
         val beanContext = BeanDefinitionContext(it)
         when (name) {
            null -> it.registerBean(T::class.java, Supplier { function.invoke(beanContext) }, customizer)
            else -> it.registerBean(name, T::class.java, Supplier { function.invoke(beanContext) }, customizer)
         }
      }
   }

   /**
    * Take in account bean definitions enclosed in the provided lambda only when the
    * specified profile is active.
    */
   fun profile(profile: String, init: BeanDefinitionDsl.() -> Unit): BeanDefinitionDsl {
      val beans = BeanDefinitionDsl({ it.activeProfiles.contains(profile) })
      beans.init()
      children.add(beans)
      return beans
   }

   /**
    * Take in account bean definitions enclosed in the provided lambda only when the
    * specified environment-based predicate is true.
    */
   fun environment(condition: (ConfigurableEnvironment) -> Boolean, init: BeanDefinitionDsl.() -> Unit): BeanDefinitionDsl {
      val beans = BeanDefinitionDsl(condition::invoke)
      beans.init()
      children.add(beans)
      return beans
   }

   override fun invoke(context: GenericApplicationContext) {
      for (registration in registrations) {
         if (condition.invoke(context.environment)) {
            registration.invoke(context)
         }
      }
      for (child in children) {
         child.invoke(context)
      }
   }
}

/**
 * Functional bean definition Kotlin DSL.
 *
 * @author Sebastien Deleuze
 * @since 5.0
 */
fun beans(init: BeanDefinitionDsl.() -> Unit): BeanDefinitionDsl {
   val beans = BeanDefinitionDsl()
   beans.init()
   return beans
}

```
