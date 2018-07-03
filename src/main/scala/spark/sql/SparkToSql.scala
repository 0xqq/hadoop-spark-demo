package spark.sql

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

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
    System.setProperty("user.name", "admin")
    //设置本地的hadoop环境变量
    if ("Windows 7".equals(osName)) {
      System.setProperty("hadoop.home.dir", "D:\\hadoop-2.6.0-cdh5.9.0")
      conf.setMaster("local[*]")
    }

    val spark = SparkSession.builder().master("local[*]").enableHiveSupport().getOrCreate()
    spark.table("stg.loan_lb_t_credit_audit_info").limit(10).show()
    spark.stop()

  }

}
