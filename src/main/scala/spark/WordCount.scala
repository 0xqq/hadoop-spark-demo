package spark

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    //设置启动的参数
    val conf: SparkConf = new SparkConf().setAppName("WordCount").setMaster("local[2]")
    //获取上下文 Context
    val sc: SparkContext = new SparkContext(conf)
    //加载 源数据
    val text = sc.textFile("hdfs://jy2tehdp01:8020/user/zouzhanshun/spark-wordcount-testdata.txt")
    //按照空格切分并压平
    val words = text.flatMap(_.split(" "))
    //把数据由 key => (key,1)映射为 (key,value)
    val pairs: RDD[(String, Int)] = words.map((_, 1))
    //调用reduceByKey算子 计算词频
    val result: RDD[(String, Int)] = pairs.reduceByKey(_ + _)
    //把 (key,value) => (value,key) 并按照value排序,然后在映射回来 (value,key) => (key,value)
    val wordCountResult: RDD[(Int, String)] = result.map(word => {
      (word._2, word._1)
    }).sortByKey(false).map(word => {
      (word._1, word._2)
    })
    //把结果输出到hdfs目录
    wordCountResult.saveAsTextFile("hdfs://jy2tehdp01:8020/user/yudafei/spark-wordcount-testdata-result")
    //并打印到屏幕上
    wordCountResult.foreach(println(_))
  }
}