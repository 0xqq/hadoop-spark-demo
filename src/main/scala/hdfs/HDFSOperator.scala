package hdfs

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

import scala.collection.mutable.ListBuffer

/**
  * Created by zls on 16-11-24.
  */
object HDFSOperator {

  def start(args: Array[String]): Unit = {
    val hdfs : FileSystem = FileSystem.get(new Configuration)
    args(0) match {
      case "list" =&gt; traverse(hdfs, args(1))
      case "createFile" =&gt; HDFSHelper.createFile(hdfs, args(1))
      case "createFolder" =&gt; HDFSHelper.createFolder(hdfs, args(1))
      case "copyfile" =&gt; HDFSHelper.copyFile(hdfs, args(1), args(2))
      case "copyfolder" =&gt; HDFSHelper.copyFolder(hdfs, args(1), args(2))
      case "delete" =&gt; HDFSHelper.deleteFile(hdfs, args(1))
      case "copyfilefrom" =&gt; HDFSHelper.copyFileFromLocal(hdfs, args(1), args(2))
      case "copyfileto" =&gt; HDFSHelper.copyFileToLocal(hdfs, args(1), args(2))
      case "copyfolderfrom" =&gt; HDFSHelper.copyFolderFromLocal(hdfs, args(1), args(2))
      case "copyfolderto" =&gt; HDFSHelper.copyFolderToLocal(hdfs, args(1), args(2))
    }
  }

  def traverse(hdfs : FileSystem, hdfsPath : String) = {
    val holder : ListBuffer[String] = new ListBuffer[String]
    val paths : List[String] = HDFSHelper.listChildren(hdfs, hdfsPath, holder).toList
    for(path &lt;- paths){
      System.out.println("--------- path = " + path)
      System.out.println("--------- Path.getname = " + new Path(path).getName)
    }
  }

}