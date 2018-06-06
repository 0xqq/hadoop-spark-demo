package spark.transformation

import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Zip {
  // 屏蔽日志
  Logger.getLogger("org.apache").setLevel(Level.WARN)

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName("zip").setMaster("local[*]")
    System.setProperty("hadoop.home.dir", "D:\\hadoop-2.6.0-cdh5.9.0")
    val sc: SparkContext = new SparkContext(conf)


    /*
    自身的RDD的值的类型为T类型，另一个RDD的值的类型为U类型。zip操作将这两个值连接在一起。
    构成一个元祖值。RDD的值的类型为元祖。
    都是第i个值和第i个值进行连接。
    zip函数用于将两个RDD组合成Key/Value形式的RDD,这里默认两个RDD的partition数量以及元素数量都相同，否则会抛出异常
     */
    val a = sc.parallelize(1 to 100, 3)
    val b = sc.parallelize(101 to 200, 3)
    val c = sc.parallelize(201 to 300, 3)
    val zipABRDD: RDD[(Int, Int)] = a.zip(b)
    zipABRDD.foreach(x => {
      println(x._1 + " : " + x._2)
    })
    val zipABCRDD: RDD[((Int, Int), Int)] = a.zip(b).zip(c)
    zipABCRDD.foreach(x => {
      println(x._1._1 + " : " + x._1._2 + " : " + x._2)
    })

    /* zipPartitions
    zipPartitions函数将多个RDD按照partition组合成为新的RDD，
    该函数需要组合的RDD具有相同的分区数，但对于每个分区内的元素数量没有要求。
    preservesPartitioning表示的是否保留父RDD的partitioner分区信息。
     */
    var rdd1 = sc.makeRDD(1 to 5, 2)
    var rdd2 = sc.makeRDD(Seq("A", "B", "C", "D", "E"), 2)

    rdd1.mapPartitionsWithIndex {
      (x, iter) => {
        var result = List[String]()
        while (iter.hasNext) {
          result ::= ("part_" + x + "|" + iter.next())
        }
        result.iterator
      }
    }.foreach(x => {
      println("x: " + x)
    })
    println("================")
    rdd2.mapPartitionsWithIndex {
      (x, iter) => {
        var result = List[String]()
        while (iter.hasNext) {
          result ::= ("part_" + x + "|" + iter.next())
        }
        result.iterator

      }
    }.foreach(x => {
      println("x: " + x)
    })
    println("================")
    rdd1.zipPartitions(rdd2) {
      (rdd1Iter, rdd2Iter) => {
        var result = List[String]()
        while (rdd1Iter.hasNext && rdd2Iter.hasNext) {
          //往集合里添加元素
          result ::= (rdd1Iter.next() + "_" + rdd2Iter.next())
        }
        result.iterator
      }
    }.foreach(x => {
      println("x: " + x)
    })
    println("================zipWithIndex")
    //zipWithIndex 表示将rdd的元素和它的索引的值进行拉练操作。索引开始于0.组合成为键值对。
    //val z = sc.parallelize(Array("A", "B", "C", "D"))
    //val r: RDD[(String, Long)] = z.zipWithIndex
    //r.foreach(x => println(x))

    val z = sc.parallelize(100 to 120, 2)
    val r = z.zipWithIndex
    r.foreach(x => println(x))
    /*
    这个表示的是给每一个元素一个新的id值，这个id值不一定和真实的元素的索引值一致。返回的同样是一个元祖
    这个唯一ID生成算法如下：
    每个分区中第一个元素的唯一ID值为：该分区索引号，
    每个分区中第N个元素的唯一ID值为：(前一个元素的唯一ID值) + (该RDD总的分区数)
     */
    println("================zipWithUniqueId")
    val p = z.zipWithUniqueId
    p.foreach(x => println(x._1 + " : " + x._2))

  }
}
