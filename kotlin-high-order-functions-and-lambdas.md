# 高阶函数、lambda表达式

- Kotlin 函数都是头等的，这意味着它们可以存储在变量与数据结构中、`作为参数`传递给其他高阶函数以及`从其他高阶函数返回`。可以像操作任何其他非函数值一样操作函数。
- Kotlin 使用一系列函数类型来表示函数并提供一组特定的`语言结构`，例如 lambda 表达式。

## 1 高阶函数 high order function
```kotlin
// 高阶函数是将函数用作参数或返回值的函数。
fun <T, R> Collection<T>.fold(
    initial: R,
    combine: (acc: R, nextElement: T) -> R
): R {
    var accumulator: R = initial
    for (element: T in this) {
        accumulator = combine(accumulator, element)
    }
    return accumulator
}
// 参数 combine 具有函数类型 (R, T) -> R , 该函数接受类型分别为 R 与 T 的两个参数并返回一个 R 类型的值。
val items = listOf(1, 2, 3, 4, 5)

// Lambdas 表达式是花括号括起来的代码块。 
// 如果一个 lambda 表达式有参数，前面是参数，后跟“->”
// lambda 表达式中的最后一个表达式是返回值.
items.fold(0, { acc: Int, i: Int ->
    val result = acc + i
    result
})
// lambda 表达式的参数类型是可选的，如果能够推断出来的话：
val joinedToString = items.fold("Elements:", { acc, i -> acc + " " + i })
// 函数引用也可以用于高阶函数调用：
val product = items.fold(1, Int::times)
```

## 2 函数类型 function type
- 函数类型具有函数签名相对应的特殊表示法，即有参数和返回值。(Int) -> String
- 所有函数类型都有一个圆括号括起来的参数类型列表以及一个返回类型：(A, B) -> C 表示接受类型分别为 A 与 B 两个参数并返回一个 C 类型值的函数类型。 参数类型列表可以为空，如 () -> A。Unit 返回类型不可省略。
- 函数类型可以有一个额外的接收者类型，它在表示法中的点之前指定： 类型 A.(B) -> C 表示可以在 A 的接收者对象上以一个 B 类型参数来调用并返回一个 C 类型值的函数。 带有接收者的函数字面值通常与这些类型一起使用。
- 挂起函数属于特殊种类的函数类型，它的表示法中有一个 suspend 修饰符 ，例如 suspend () -> Unit 或者 suspend A.(B) -> C。
- 函数类型表示法可以选择性地包含函数的参数名：(x: Int, y: Int) -> Point。x，y 参数名称可用于表明参数的含义。
```text
1. 如需将函数类型指定为可空，请使用圆括号：((Int, Int) -> Int)?
2. 函数类型可以使用圆括号进行接合：(Int) -> ((Int) -> Unit)
3. 箭头表示法是右结合的，(Int) -> (Int) -> Unit 与前述示例等价，但不等于 ((Int) -> (Int)) -> Unit。
```
- 可以通过使用类型别名给函数类型起一个别称
```kotlin
typealias ClickHandler = (Button, ClickEvent) -> Unit
```
### 2.1 函数类型实例化
获得函数类型的实例有以下途径：
- 使用函数字面值的代码块：lambda表达式 `{ a,b -> a + b }` 、匿名函数 `fun(s: String): Int { return s.toIntOrNull() ?: 0}`
- 使用已有声明的可调用引用：顶层、局部、扩展函数 `::isOdd` `String::toInt` 、 顶层、成员、扩展属性 `List<Int>::size` 、 构造函数 `::Regex` (包括指向特定实例成员的绑定可调用的引用`foo::toString`)
- 使用实现函数类型接口的自定义类的实例
```kotlin
class IntTransformer: (Int) -> Int{
    override override fun invoke(x: Int): Int = TODO()
}
val intFunction: (Int) -> Int = IntTransformer()
// 1 如果有足够信息，编译器可以推断变量的函数类型：
val a = { i: Int -> i + 1 } // 推断出的类型是 (Int) -> Int
// 2 带与不带接收者的函数类型非字面值可以互换，其中接收者可以替代第一个参数，反之亦然。
// 例如：(A, B) -> C 类型的值可以传给或赋值给期待 A.(B) -> C 的地方，反之亦然。
// 请注意，默认情况下推断出的是没有接收者的函数类型，即使变量是通过扩展函数引用来初始化的。 如需改变这点，请显式指定变量类型。
val repeatFun: String.(Int) -> String = { times -> this.repeat(times) }
val twoParameters: (String, Int) -> String = repeatFun // OK

fun runTransformation(f: (String, Int) -> String): String {
    return f("hello", 3)
}
val result = runTransformation(repeatFun) // OK
```

### 2.2 函数类型实例调用
函数类型的值可以通过其 `invoke(……)` 操作符调用：`f.invoke(x)` 或者直接 `f(x)`。
```kotlin
// 如果该值具有接收者类型，那么应该将接收者对象作为第一个参数传递。 
// 调用带有接收者的函数类型值的另一个方式是在其前面加上接收者对象， 就好比该值是一个扩展函数：1.foo(2)
val stringPlus: (String, String) -> String = String::plus
val intPlus: Int.(Int) -> Int = Int::plus

println(stringPlus.invoke("<-", "->"))
println(stringPlus("Hello, ", "world!"))

println(intPlus.invoke(1, 1))
println(intPlus(1, 2))
println(2.intPlus(3)) // 类扩展调用
```

### 2.3 内联函数
有时使用内联函数可以`为高阶函数`提供灵活的`控制流`。

## 3 lambda表达式和匿名函数

### 3.1 lambda表达式
- lambda 表达式与匿名函数是`函数字面值`，即未声明的函数， 但立即做为表达式传递。 `max(strings, { a, b -> a.length < b.length })`
- Lambda 表达式的完整语法形式: `val sum: (Int, Int) -> Int = { x: Int, y: Int -> x + y }`。lambda 表达式总是括在花括号中， 完整语法形式的参数声明放在花括号内，并有可选的类型标注， 函数体跟在一个 -> 符号之后。
- 如果推断出的该 lambda 的返回类型不是 Unit，那么该 lambda 主体中的最后一个（或可能是单个） 表达式会视为返回值。`val sum = { x: Int, y: Int -> x + y }`
- 如果函数的最后一个参数是函数，那么作为相应参数传入的 lambda 表达式可以放在圆括号之外。`val product = items.fold(1) { acc, e -> acc * e }`
- 如果该 lambda 表达式是调用时唯一的参数，那么圆括号可以完全省略。 `run { println("...") }`
- 如果编译器自己可以识别出签名，也可以不用声明唯一的参数并忽略 ->。 该参数会隐式声明为 it。`ints.filter { it > 0 } // 这个字面值是“(it: Int) -> Boolean”类型的`
```kotlin
// 可以使用`限定的返回语法`从 lambda 显式返回一个值。 否则，将隐式返回最后一个表达式的值。
ints.filter {
    val shouldFilter = it > 0
    shouldFilter
}

ints.filter {
    val shouldFilter = it > 0
    return@filter shouldFilter  // warn: 这里需要`限定的返回语法` 才能从lambda表达式中返回
}
// 如果 lambda 表达式的参数未使用，那么可以用下划线取代其名称
map.forEach { _, value -> println("$value!") }
```

### 3.2 匿名函数
- lambda 表达式语法缺少的一个东西是指定函数的返回类型的能力。`在大多数情况下，这是不必要的`。因为返回类型可以自动推断出来。然而，如果确实`需要显式指定`，可以使用另一种语法：`匿名函数` 。
- 匿名函数看起来非常像一个常规函数声明，除了其名称省略了。其函数体可以是表达式或代码块。
- 匿名函数扽参数和返回类型的指定方式与常规函数相同，除了能够从上下文推断出的参数类型可以省略
- 匿名函数的返回类型推断机制与正常函数一样：对于具有表达式函数体的匿名函数将自动推断返回类型，而具有代码块函数体的返回类型必须显式指定（或者已假定为 Unit）。

```kotlin
fun(x: Int, y: Int): Int = x + y
fun(x: Int, y: Int): Int {
    return x + y
}
ints.filter(fun(item) = item > 0)
```

### 3.3 lambda和匿名函数的区别
- 匿名函数参数总是在括号内传递。 允许将函数留在圆括号外的简写语法仅适用于 lambda 表达式。
- Lambda表达式与匿名函数之间的另一个区别是非局部返回的行为。一个不带标签的 return 语句总是在用 fun 关键字声明的函数中返回。这意味着 lambda 表达式中的 return 将从包含它的函数返回，而匿名函数中的 return 将从匿名函数自身返回。

### 3.4 闭包
Lambda 表达式或者匿名函数（以及局部函数和对象表达式） 可以访问其 闭包 ，即在外部作用域中声明的变量。 在 lambda 表达式中可以修改闭包中捕获的变量
```kotlin
var sum = 0
ints.filter { it > 0 }.forEach {
    sum += it
}
print(sum)
```

### 3.5 带有接收者的函数字面值
带有接收者的函数类型，例如 `A.(B) -> C`，可以用特殊形式的函数字面值实例化 —— 带有接收者的函数字面值。
> Kotlin 提供了调用带有接收者（提供接收者对象）的函数类型实例的能力。
> 在这样的函数字面值内部，传给调用的接收者对象成为隐式的this，以便访问接收者对象的成员而无需任何额外的限定符，亦可使用 this 表达式 访问接收者对象。
> 这种行为与扩展函数类似，扩展函数也允许在函数体内部访问接收者对象的成员。
> 匿名函数语法允许你直接指定函数字面值的接收者类型。 
> 
```kotlin
// 带有接收者的函数字面值及其类型的示例，其中在接收者对象上调用了 plus
val sum: Int.(Int) -> Int = { other -> plus(other) }
// 如果你需要使用带接收者的函数类型声明一个变量，并在之后使用它，这将非常有用。
val sum = fun Int.(other: Int): Int = this + other
```
当接收者类型可以从上下文推断时，lambda 表达式可以用作带接收者的函数字面值。
```kotlin
class HTML {
    fun body() {  }
}

fun html(init: HTML.() -> Unit): HTML {
    val html = HTML()  // 创建接收者对象
    html.init()        // 将该接收者对象传给该 lambda
    return html
}

html {       // 带接收者的 lambda 由此开始
    body()   // 调用该接收者对象的一个方法
}
```









