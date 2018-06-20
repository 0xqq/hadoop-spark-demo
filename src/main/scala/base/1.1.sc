
// 1.1sacal解释器
8 * 5 + 2

"Hello,".toLowerCase
"Hello,".toUpperCase
//1.2 声明值和变量

//val 声明的不可变
val answer = 8 * 5 + 2

0.5 * answer
//var 声明的可变
var counter = 0
counter = 1

//推荐使用val 不需要给出变量的类型。可以自动推荐类型

//val greeting: String = null
val greeting: Any = "Hello"
//变量和函数的类型写在总是写在变量和函数的后面
//不需要用

val arr = Array[Int](1, 2, 3, 4, 5)

for (ele <- arr) {
  println(ele)
}
//遍历数据组
val index = Array(0, 1, 2, 3, 4)
for (i <- index) {
  println(arr(i))
}

1 to 10
1.to(10)

for (ele <- 0 until arr.length) {
  println(arr(ele))
}
//取出数组的偶数
for (ele <- arr) {
  if (ele % 2 == 0) {
    println(ele)
  }
}

for (ele <- arr; if ele % 2 == 0) {
  println("偶数：" + ele)
}

for (i <- 1 to 10; j <- 1 to 10; if i != j) {
  println(10 * i + j + " ")
}

val r1 = for (ele <- arr if ele % 2 == 0) yield ele

class Student(val name: String, var age: Int)

val s1 = new Student("zhangsan", 18)
val s2 = new Student("lisi", 20)

//val list = new List[Student]()

var students: List[Student] = List()
var s1s= students :+ s1
var s2s = s1s :+ s2

println("aaa: " + s2s.size)

val filterList = for (student <- s2s; if student.age > 19) yield student




