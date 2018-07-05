package spark

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

object WordCount2 {
  // 屏蔽日志
  Logger.getLogger("org.apache").setLevel(Level.WARN)

  def main(args: Array[String]): Unit = {

    //设置启动的参数
    val conf: SparkConf = new SparkConf().setAppName("WordCount")
    //获得系统属性集
    val props = System.getProperties();
    //操作系统名称
    val osName = props.getProperty("os.name");
    //设置本地的hadoop环境变量
    if ("Windows 7".equals(osName)) {
      System.setProperty("hadoop.home.dir", "D:\\hadoop-2.6.0-cdh5.9.0")
      System.setProperty("HADOOP_USER_NAME", "root")
      conf.setMaster("local[*]")
    }

    // 获取上下文 Context
    val sc: SparkContext = new SparkContext(conf)

    val text = sc.textFile("hdfs://nameservice1:8020/user/zouzhanshun/spark-wordcount-testdata.txt")

    val counts = text.flatMap(line => line.split(" ")).map(word => (word, 1)).reduceByKey(_ + _)
    //如果存在就删除 把core-site.xml hadoop-env.sh hdfs-site.xml ssl-client.xml 四个配置文件放到resources目录下
    val hdfs: FileSystem = FileSystem.get(new Configuration)
    val path = new Path("/user/zouzhanshun/spark-wordcount-testdata-result")
    if(hdfs.exists(path)){
      hdfs.delete(path)
    }
    //counts.saveAsTextFile("hdfs://nameservice1:8020/user/zouzhanshun/spark-wordcount-testdata-result")
    counts.foreach(println(_))
  }

}
