package spark.sql

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{Dataset, Row, SparkSession}

object SparkToSql {

  // 屏蔽日志
  Logger.getLogger("org.apache").setLevel(Level.WARN)

  def main(args: Array[String]): Unit = {

    //设置启动的参数
    val conf: SparkConf = new SparkConf().setAppName("SparkToSql")
    //获得系统属性集
    val props = System.getProperties();
    //操作系统名称
    val osName = props.getProperty("os.name");

    //设置本地的hadoop环境变量
    if ("Windows 7".equals(osName)) {
      System.setProperty("user.name", "admin")
      System.setProperty("hadoop.home.dir", "D:\\hadoop-2.6.0-cdh5.9.0")
      conf.setMaster("local[*]")
    }

    val spark = SparkSession.builder().enableHiveSupport().getOrCreate()
    val reslut: Dataset[Row] = spark.table("stg.loan_lb_t_credit_audit_info").limit(10)
    reslut.show()
    val hdfs: FileSystem = FileSystem.get(new Configuration)
    val path = new Path("/user/zouzhanshun/spark-pi-testdata-result/")
    if (hdfs.exists(path)) {
      hdfs.delete(path,true)
    }
    //reslut.rdd.saveAsTextFile("/user/zouzhanshun/spark-pi-testdata-result/")
    spark.stop()

  }

}
