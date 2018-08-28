package io

import java.io._
import java.nio._

import scala.io.{BufferedSource, Source}

object ScalaIoDemo {
  def main(args: Array[String]): Unit = {

    //write("a.txt","D:","a little bit long ...")
    read("a.txt", "D:")
    val file: File = new java.io.File("D:\\a.txt")
    if (file.exists) {
      System.out.println("file exists")
      rename(file)
    }
    else {
      System.out.println("file not exists, create it ...")

    }
  }

  def write(fileName: String, filePath: String, str: String): Unit = {
    val f = new FileOutputStream(filePath + "\\" + fileName).getChannel
    f write ByteBuffer.wrap(str.getBytes)
    f close
  }

  def read(fileName: String, filePath: String): Unit = {
    //方式一 逐行读文件内容：
    val file: File = new java.io.File(filePath + "\\" + fileName)
    val source: BufferedSource = Source.fromFile(file)
    val Lines: Iterator[String] = source.getLines()
    Lines.foreach(println)
    println("=========================================================")
    //方式二
    val txt = Source.fromFile(filePath + "\\" + fileName).mkString
    println(txt)
  }

  def rename(oldFile: File): Boolean = {
    val newFileName = oldFile.getName + System.currentTimeMillis()
    val newFilePath = oldFile.getPath
    val str: String = System.getProperty("file.separator")
    val newFile = new File(newFilePath + str + newFileName)
    oldFile.renameTo(newFile);
  }

}
