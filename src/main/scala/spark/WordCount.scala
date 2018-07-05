package spark

import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object WordCount {

  // 屏蔽日志
  Logger.getLogger("org.apache").setLevel(Level.WARN)

  def main(args: Array[String]): Unit = {

    //设置启动的参数
    val conf: SparkConf = new SparkConf().setAppName("WordCount")
    //获得系统属性集
    val props = System.getProperties();
    //操作系统名称
    val osName = props.getProperty("os.name");
    System.setProperty("user.name", "dell")
    //设置本地的hadoop环境变量
    if ("Windows 7".equals(osName)) {
      System.setProperty("hadoop.home.dir", "D:\\hadoop-2.6.0-cdh5.9.0")
      System.setProperty("HADOOP_USER_NAME", "root")
      conf.setMaster("local[*]")
    }

    // 获取上下文 Context
    val sc: SparkContext = new SparkContext(conf)
    // 加载 源数据
    // 注意:nameservice1可以是主机名称
    // 也可以是HDFS 命名服务的逻辑名称
    // 在core-site.xml里面配置的
    /*
      <property>
        <name>fs.defaultFS</name>
        <value>hdfs://nameservice1</value>
      </property>
     */
    // 本地运行必须放入resources目录下应用会自动加载core-site.xml
    val text = sc.textFile("hdfs://nameservice1:8020/user/zouzhanshun/spark-wordcount-testdata.txt")
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
    //wordCountResult.saveAsTextFile("hdfs://nameservice1:8020/user/zouzhanshun/spark-wordcount-testdata-result")
    //并打印到屏幕上
    wordCountResult.foreach(println(_))
  }
}