package hbase;

import hbase.utils.PropertiesUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.io.IOException;
import java.util.Properties;

public class HBaseConnectionUtils {

    public static Connection getConnection() throws IOException {
        //获得系统属性集
        Properties props = System.getProperties();
        //操作系统名称
        String osName = props.getProperty("os.name");
        //设置本地的hadoop环境变量
        if ("Windows 7".equals(osName)) {
            System.setProperty("HADOOP.HOME.DIR", "D:\\hadoop-2.6.0-cdh5.9.0");
            System.setProperty("HADOOP_USER_NAME", "dell");
            System.out.println("====================>已设置hadoopdir");
        }
        Connection connection = ConnectionFactory.createConnection(getConfiguration());
        return connection;
    }

    private static Configuration getConfiguration() throws IOException {

        Properties props = PropertiesUtils.load("hbase.properties");

        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.property.clientPort", props.getProperty("hbase.zookeeper.property.clientPort"));
        config.set("hbase.zookeeper.quorum", props.getProperty("hbase.zookeeper.quorum"));
        return config;
    }
}
