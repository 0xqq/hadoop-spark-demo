package hadoop.script;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Properties;
//TODO hdfs文件的操作 环境变量的设置 提交job的conf的设置
public class CatDemo_0010 {
    public static void main(String[] args) throws IOException {
        String path = Thread.currentThread().getContextClassLoader().getResource("hadoop.properties").getPath();
        Properties p = new Properties();
        InputStream is = new FileInputStream(new File(path));
        p.load(is);
        String user = p.getProperty("hadoop.user.name");


        final Map<String, String> getenv = System.getenv();
        for (String str : getenv.keySet()) {
            System.out.println(str + ":" + getenv.get(str));
        }

        //以下两行用来指明登陆hadoop的用户和你本地的hadoop-2.6.0所存的目录。
        System.setProperty("HADOOP_USER_NAME", "dell");
        System.setProperty("hadoop.home.dir", "D:\\hadoop-2.6.0-cdh5.9.0");

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
    }
}