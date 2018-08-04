package kafka;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KafkaConsumerConfig {


    public static String getZookeeperConn() {
        String res = null;
        try {
            res = init();
        } catch (Exception e) {
            System.out.println(e);
        }
        return res;
    }

    public static String init() throws IOException {
        //加载配置参数
        String path = Thread.currentThread().getContextClassLoader().getResource("application.properties").getPath();
        Properties p = new Properties();
        InputStream is = new FileInputStream(new File(path));
        p.load(is);
        return p.get("zk").toString();
    }


}
