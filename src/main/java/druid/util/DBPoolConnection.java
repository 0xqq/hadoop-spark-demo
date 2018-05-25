package druid.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 要实现单例模式，保证全局只有一个数据库连接池
 *
 * @author admin
 * <p>
 * 2016年10月21日
 */
public class DBPoolConnection {
    private static DBPoolConnection dbPoolConnection = null;
    private static DruidDataSource druidDataSource = null;

    static {
        /* 方法一 调用方法加载
        Properties properties = loadPropertiesFile("classes/mysql_server.properties");
         */

        /* 方法二 调用方法加载
        获取文件的真是路径
        Thread.currentThread().getContextClassLoader().getResource("application.properties").getPath();
        根据真是路径构建一个流
        InputStream in=new FileInputStream(new File(path));
        把该流加载成为一个Properties
        Properties props = new Properties();
        props.load(in);
         */

        /* 方法三 调用已经封装的流
        getResourceAsStream 直接获得一个流，是对上述的封装
        加载的是resources目录下的文件，但是为什么不加 dev、online、test 是因为maven在编译的时候会去掉相应的目录
        在系统的目录是D:\workspace_idea\kafkaDemo\target\classes\mysql_server.properties
        当然也可以在resources下新建一个文件夹
         */

        Properties properties = new Properties();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("mysql_server.properties");
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);    //DruidDataSrouce工厂模式
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 数据库连接池单例
     *
     * @return
     */
    public static synchronized DBPoolConnection getInstance() {
        if (null == dbPoolConnection) {
            dbPoolConnection = new DBPoolConnection();
        }
        return dbPoolConnection;
    }

    /**
     * 返回druid数据库连接
     *
     * @return
     * @throws SQLException
     */
    public DruidPooledConnection getConnection() throws SQLException {
        return druidDataSource.getConnection();
    }

    /**
     * @param fullFile 配置文件名
     * @return Properties对象
     */
    private static Properties loadPropertiesFile(String fullFile) {
        String webRootPath = null;
        if (null == fullFile || fullFile.equals("")) {
            throw new IllegalArgumentException("Properties file path can not be null" + fullFile);
        }
        webRootPath = DBPoolConnection.class.getClassLoader().getResource("").getPath();
        webRootPath = new File(webRootPath).getParent();
        System.out.println("webRootPath: " + webRootPath);
        InputStream inputStream = null;
        Properties p = null;
        try {
            inputStream = new FileInputStream(new File(webRootPath + File.separator + fullFile));
            p = new Properties();
            p.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return p;
    }

}