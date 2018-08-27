package spark.transformation

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

object Test {

  // 屏蔽日志
  Logger.getLogger("org.apache").setLevel(Level.WARN)

  def main(args: Array[String]): Unit = {
/*
* INFO 206-07-25 00:29:53 requestURL:/c?app=0&did=18005472&industry=469&adid=31
INFO 206-07-25 00:29:53 requestURL:/c?app=0&did=18005472&industry=469&adid=31
INFO 206-07-25 00:29:53 requestURL:/c?app=0&did=18005472&industry=469&adid=32
* */
    //设置启动的参数
    val conf: SparkConf = new SparkConf().setAppName("WordCount")
    //获得系统属性集
    val props = System.getProperties();
    //操作系统名称
    val osName = props.getProperty("os.name");
    //设置本地的hadoop环境变量
    if ("Windows 7".equals(osName)) {
      System.setProperty("HADOOP.HOME.DIR", "D:\\hadoop-2.6.0-cdh5.9.0")
      System.setProperty("HADOOP_USER_NAME", "dell")
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
    val text = sc.textFile("D:\\1.txt",3)
    //按照空格切分并压平
    val words = text.flatMap(_.split("\t"))
    val word: RDD[String] = words.filter(_.endsWith("adid=31"))
    word.collect().foreach(println(_))

/*    //把数据由 key => (key,1)映射为 (key,value)
    val pairs: RDD[(String, Int)] = words.map((_, 1))
    //调用reduceByKey算子 计算词频
    val result: RDD[(String, Int)] = pairs.reduceByKey(_ + _)
    //把 (key,value) => (value,key) 并按照value排序,然后在映射回来 (value,key) => (key,value)
    val wordCountResult: RDD[(Int, String)] = result.map(word => {
      (word._2, word._1)
    }).sortByKey(false).map(word => {
      (word._1, word._2)
    })
    //如果存在就删除 把core-site.xml hadoop-env.sh hdfs-site.xml ssl-client.xml 四个配置文件放到resources目录下
    val hdfs: FileSystem = FileSystem.get(new Configuration)
    val path = new Path("/user/zouzhanshun/spark-wordcount-testdata-result")
    if (hdfs.exists(path)) {
      hdfs.delete(path, true)
    }
    //把结果输出到hdfs目录
    wordCountResult.saveAsTextFile("/user/zouzhanshun/spark-wordcount-testdata-result")
    //并打印到屏幕上
    wordCountResult.foreach(println(_))*/
  }
}