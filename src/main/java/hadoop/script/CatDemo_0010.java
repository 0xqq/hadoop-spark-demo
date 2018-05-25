package hadoop.script;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

public class CatDemo_0010 {
    public static void main(String[] args) throws IOException {
        String path = Thread.currentThread().getContextClassLoader().getResource("hadoop.properties").getPath();
        Properties p = new Properties();
        InputStream is = new FileInputStream(new File(path));
        p.load(is);
        String user = p.getProperty("hadoop.user.name");
        System.setProperty("user.name", user);
        String server = p.getProperty("hadoop.server.ip");
        String port = p.getProperty("hadoop.server.hdfs.port");


        String url = "hdfs://" + server + ":" + port + "/user/zouzhanshun/spark-wordcount-testdata.txt";
        System.out.println("url: " + url);
        // 创建Configuration对象
        Configuration conf = new Configuration();
        //conf.addResource(url);
        // 创建FileSystem对象
        FileSystem fs =
                FileSystem.get(URI.create(url), conf);
        // 需求：查看/user/kevin/passwd的内容
        // args[0] hdfs://1.0.0.5:9000/user/zyh/passwd
        // args[0] file:///etc/passwd
        FSDataInputStream fsi =
                fs.open(new Path(args[0]));
        byte[] buff = new byte[1024];
        int length = 0;
        while ((length = fsi.read(buff)) != -1) {
            System.out.println(
                    new String(buff, 0, length));
        }
        System.out.println(
                fs.getClass().getName());
    }
}