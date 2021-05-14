# Functions

## 1 Function 函数

### 1.1 函数声明
```kotlin
// 使用关键词 fun
fun double(x: Int): Int{
    return 2 * x
}
```

### 1.2 函数用法
```kotlin
// 1 调用函数使用传统的方法
// 2 调用成员函数使用点表示法, 创建类Stream实例并调用read()
val result = double(2);
Stream().read()
// 3 函数参数使用Pascal表示法定义, 即"name: type" 参数用逗号隔开。每个参数必须显式类型
// 4 声明函数参数时，可以使用尾部逗号
// 5 函数参数可以有默认值，当省略相应参数时使用默认值。减少函数重载数量。
// 6 Overriding 方法"总是使用"与基类相同的默认参数值。当覆盖一个带默认值的方法时，必须从签名中省略默认参数值。
// 7 如果一个默认参数在一个无默认值的参数之前，那么该默认值只能通过使用具名参数调用该函数来使用。
// 8 如果在默认参数之后的最后一个参数是 lambda 表达式，那么它既可以作为具名参数在括号内传入，也可以在括号外传入。
fun powerOf(number: Int, exponent: Int): Int{ }
fun powerOf(
    number: Int,
    exponent: Int, // trailing comma
){}
fun read(
    b: Array<Byte>,
    off: Int = 0,
    len: Int = b.size,
){}
open class A {
    open fun foo(i: Int = 10){}
}
open class B: A(){
    override fun foo(i: Int){} // warn: 不能有默认值
}
fun foo(
    bar: Int = 0,
    baz: Int,
){}
foo(baz = 1) // 使用默认 bar = 0
fun foo(
    bar: Int = 0,
    baz: Int = 1,
    qux: () -> Unit,
){}
foo(1){ println("hello") }      // 使用默认值 baz = 1
foo(qux = { println("hello") }) // 使用两个默认值 bar = 0 与 baz = 1
foo(){ println("hello") }       // 使用两个默认值 bar = 0 与 baz = 1
foo{ println("hello") }         // 使用两个默认值 bar = 0 与 baz = 1
// 9 当在函数调用中使用命名参数时，可以自由更改它们的列出顺序，如果要使用它们的默认值，则可以将它们全部排除在外。
// 10 可以使用`spread`运算符`*`将可变数量的参数（vararg）与名称一起传递
// 11 对于 JVM 平台：在`调用 Java 函数`时不能使用具名参数语法，因为 Java 字节码并不总是保留函数参数的名称。
fun reformat(
    str: String,
    normalizeCase: Boolean = true,
    upperCaseFirstLetter: Boolean = true,
    divideByCamelHumps: Boolean = false,
    wordSeparator: Char = ' ',
) {}
reformat(
    'String!',
    false,
    upperCaseFirstLetter = false,
    divideByCamelHumps = true,
    '_'
) // 调用此函数时，不必命名其所有参数.
reformat('This is a long String!') // 可以跳过所有带有默认值的参数
reformat('This is a short String!', upperCaseFirstLetter = false, wordSeparator = '_') // 可以跳过一些带有默认值的参数。但是，在第一个跳过的参数之后，必须命名所有后续参数
fun foo(vararg strings: String) { /*……*/ }
foo(strings = *arrayOf("a", "b", "c"))
// 12 如果一个函数不返回任何有用的值，它的返回类型是 Unit。Unit 是一种只有一个值——Unit 的类型。这个值不需要显式返回。
// 13 Unit 返回类型声明也是可选的。
fun printHello(name: String?): Unit{
    if (name != null) {
        println("hello $name")
    }else{
        println("hi here") // 或者 `return Unit` 或者 `return` 都可以
    }
}
fun printHello(name: String?) { }
// 14 当函数返回单个表达式时， 可以省略花括号并在 "=" 符号之后指定代码体即可。
// 15 当返回值类型可由编译器推断时，显式声明返回类型是可选的。
// 16 具有块代码体的函数必须始终显式指定返回类型，除非他们旨在返回 Unit，在这种情况下它是可选的。
// Kotlin 不推断具有块代码体的函数的返回类型，因为这样的函数在代码体中可能有复杂的控制流，并且返回类型对于读者（有时甚至对于编译器）是不明显的。
fun double(x: Int): Int = x * 2
fun double(x: Int) = x * 2
// 17 函数的参数（通常是最后一个）可以用 vararg 修饰符标记。允许将可变数量的参数传递给函数
// 18 只有一个参数可以标注为 vararg。如果 vararg 参数不是列表中的最后一个参数， 可以使用具名参数语法传递其后的参数的值，或者，如果参数具有函数类型，则通过在括号外部传一个 lambda。
fun <T> asList(vararg ts: T): List<T> {
    val result = ArrayList<T>()
    for (t in ts) // ts is an Array
        result.add(t)
    return result
}
val list = asList(1, 2, 3)
// 19 infix 中缀表示法，标有 infix 关键字的函数也可以使用中缀表示法（忽略该调用的点与圆括号）调用。中缀函数必须满足以下要求
// 19.1 必须是成员函数或扩展函数
// 19.2 必须只有一个参数 
// 19.3 其参数不得接受可变数量的参数并不能有默认值
// 20 中缀函数调用的优先级低于算术操作符、类型转换以及 rangeTo 操作符。
// 21 另一方面，中缀函数调用的优先级高于布尔操作符 && 与 ||、is- 与 in- 检测以及其他一些操作符。这些表达式也是等价的。
// 22 中缀函数总是要求指定接收者与参数。当使用中缀表示法在当前接收者上调用方法时，需要显式使用 this；不能像常规方法调用那样省略。这是确保非模糊解析所必需的。
infix fun Int.shl(x: Int): Int {}
1 shl 2 // 等同 1.shl(2)
1 shl 2 + 3 // 等同 1 shl (2 + 3)
a && b xor c // 等同  a && (b xor c)
class MyStringCollection{
    infix fun add(s: String){ }
    fun build(){
        this add "abc"
        add("abc")
        // add "abc" 是错误的调用，必须指定接收者
    }
}
```

### 1.3 函数作用域
```kotlin
// 1 函数可以在文件顶层声明
// 2 函数也可以声明在局部作用域、作为成员函数以及扩展函数。
// 3 局部函数, 一个函数在另一个函数内部。局部函数可以访问外部函数（即闭包）的局部变量。
// 4 成员函数, 在类或对象内部定义的函数。成员函数以点表示法调用。
fun dfs(graph: Graph) {
    fun dfs(current: Vertex, visited: MutableSet<Vertex>) {
        if (!visited.add(current)) return
        for (v in current.neighbors)
            dfs(v, visited)
    }
    dfs(graph.vertices[0], HashSet())
}
fun dfs(graph: Graph) {
    val visited = HashSet<Vertex>() // 局部变量
    fun dfs(current: Vertex) {
        if (!visited.add(current)) return
        for (v in current.neighbors)
            dfs(v)
    }

    dfs(graph.vertices[0])
}
class Sample {
    fun foo() { print("Foo") }
}
Sample().foo() // 创建类 Sample 实例并调用 foo
```

### 1.4 范型函数
```kotlin
// 函数可以有泛型参数，通过在函数名前使用尖括号指定
fun <T> singletonList(item: T): List<T> { }
```

### 1.5 内联函数
见其他

### 1.6 扩展函数
见其他

### 1.7 高阶函数、lambada表达式
见其他

### 1.8 尾递归函数
```kotlin
// Kotlin 支持一种称为尾递归的函数式编程风格。 这允许一些通常用循环写的算法改用递归函数来写，而无堆栈溢出的风险。 
// 当一个函数用 tailrec 修饰符标记并满足所需的形式时，编译器会优化该递归，留下一个快速而高效的基于循环的版本：
// 要符合 tailrec 修饰符的条件的话，函数必须将其自身调用作为它执行的最后一个操作。
// 在递归调用后有更多代码时，不能使用尾递归，并且不能用在 try/catch/finally 块中。目前在 Kotlin for JVM 与 Kotlin/Native 中支持尾递归。
val eps = 1E-10 // "good enough", could be 10^-15

tailrec fun findFixPoint(x: Double = 1.0): Double
        = if (Math.abs(x - Math.cos(x)) < eps) x else findFixPoint(Math.cos(x))
```