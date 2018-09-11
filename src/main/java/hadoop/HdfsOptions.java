package hadoop;

import base.SystemBase;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.*;
import java.net.URI;

public class HdfsOptions {


    private static String HDFSUri = "";
//    private static String HDFSUri = "hdfs://172.18.101.133:8020";

    /**
     * 1、获取文件系统
     *
     * @retrun FileSystem 文件系统
     */

    public static FileSystem getFileSystem() {

        //读取配置文件
        Configuration conf = new Configuration();
        //文件系统
        FileSystem fs = null;
        String hdfsUri = HDFSUri;
        if (StringUtils.isBlank(hdfsUri)) {
            //返回默认文件系统，如果在hadoop集群下运行，使用此种方法可直接获取默认文件系统；
            try {
                fs = FileSystem.get(conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //返回指定的文件系统，如果在本地测试，需要此种方法获取文件系统；
            try {
                URI uri = new URI(hdfsUri.trim());
                fs = FileSystem.get(uri, conf);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fs;
    }


    /**
     * 2、创建文件目录
     *
     * @param path 文件路径
     */
    public static void mkdir(String path) {

        try {
            FileSystem fs = getFileSystem();
            System.out.println("已创建目录: " + path);
            //创建目录
            fs.mkdirs(new Path(path));
            //释放资源
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 3、判断目录是否存在
     *
     * @param filePath 目录路径
     * @param create   若不存在是否创建
     */
    public static boolean existDir(String filePath, boolean create) {

        boolean flag = false;

//        if (StringUtils.isNotEmpty(filePath)) {
//            return flag;
//        }

        try {
            Path path = new Path(filePath);
            //FileSystem对象
            FileSystem fs = getFileSystem();
            if (create) {
                if (!fs.exists(path)) {
                    fs.mkdirs(path);
                    System.out.println("不存在" + path + "创建完成");
                }
            }

            if (fs.isDirectory(path)) {
                flag = true;
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        return flag;

    }

    /**
     * 4、本地文件上传至HDFS
     *
     * @param srcFile  源文件路径
     * @param destPath 目的文件路径
     */

    public static void copyFileToHDFS(String srcFile, String destPath) throws Exception {

        FileInputStream fis = new FileInputStream(new File(srcFile));//读取本地文件
        Configuration config = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(HDFSUri + destPath), config);
        OutputStream os = fs.create(new Path(destPath));
        //cpoy
        IOUtils.copyBytes(fis, os, 4096, true);

        System.out.println("copy " + srcFile + "到" + destPath + "完成 ......");
        fs.close();
    }

    /**
     * 5、从HDFS下载文件到本地
     *
     * @param srcFile  源文件路径
     * @param destPath 目的文件路径
     */
    public static void getFile(String srcFile, String destPath) throws Exception {

        //HDFS文件地址
        String file = HDFSUri + srcFile;
        Configuration config = new Configuration();
        //构建filesystem
        FileSystem fs = FileSystem.get(URI.create(file), config);
        //读取文件
        InputStream is = fs.open(new Path(file));
        IOUtils.copyBytes(is, new FileOutputStream(new File(destPath)), 2048, true);
        System.out.println("下载" + srcFile + "到" + destPath + "完成......");
        fs.close();
    }

    /**
     * 6、删除文件或者文件目录
     *
     * @param path
     */
    public static void rmdir(String path) {

        try {
            //返回FileSystem对象
            FileSystem fs = getFileSystem();

            String hdfsUri = HDFSUri;
            if (StringUtils.isNotBlank(hdfsUri)) {
                path = hdfsUri + path;
            }
            //删除文件或者文件目录 delete(Path f)此方法已经弃用
            System.out.println("已删除: " + fs.delete(new Path(path), true));
            fs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 7、读取文件的内容
     *
     * @param filePath
     * @throws IOException
     */
    public static void readFile(String filePath) throws IOException {

        Configuration config = new Configuration();
        String file = HDFSUri + filePath;
        FileSystem fs = FileSystem.get(URI.create(file), config);
        //读取文件
        InputStream is = fs.open(new Path(file));
        //读取文件
        IOUtils.copyBytes(is, System.out, 2048, false); //复制到标准输出流
        fs.close();
    }


    /**
     * 主方法测试
     */
    public static void main(String[] args) throws Exception {       //连接fs

        SystemBase.setOS();

        FileSystem fs = getFileSystem();
        System.out.println(fs.getUsed());
        //创建路径
        mkdir("/dit2");
        //验证是否存在
        System.out.println("/dit2是否存在: " + existDir("/dit2", true));
        //上传文件到HDFS
        copyFileToHDFS("E:\\data\\data.txt", "/dit/HDFSTest.txt");
        //下载文件到本地
        getFile("/dit/HDFSTest.txt", "E:\\HDFSTest.txt");
        //删除文件
        rmdir("/dit2");
        //读取文件
        readFile("/dit/HDFSTest.txt");
    }

}