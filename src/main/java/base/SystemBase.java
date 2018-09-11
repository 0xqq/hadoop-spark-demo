package base;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

@Slf4j
public class SystemBase {

    public static void setOS() {

        Logger logger = LoggerFactory.getLogger(SystemBase.class);
        Properties props = System.getProperties();
        //操作系统名称
        String osName = props.getProperty("os.name");
        //设置本地的hadoop环境变量
        if ("Windows 7".equals(osName)) {
            System.setProperty("hadoop.home.dir", "D:\\hadoop-2.6.0-cdh5.9.0");
            //用户欺骗
            System.setProperty("HADOOP_USER_NAME", "hdfs");
            logger.info("--------set--hadoop.home.dir=D:\\hadoop-2.6.0-cdh5.9.0-----------");
            logger.info("--------set--HADOOP_USER_NAME=hdfs-------------");
        }
    }
}