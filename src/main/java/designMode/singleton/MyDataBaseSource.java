package designMode.singleton;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public enum MyDataBaseSource {
    DATASOURCE;
    private ComboPooledDataSource cpds = null;

    private MyDataBaseSource() {
        try {

            /*--------获取properties文件内容------------*/
            // 方法一:
            InputStream is = MyDataBaseSource.class.getClassLoader().getResourceAsStream("jdbc.propertis");
            Properties p = new Properties();
            p.load(is);
            cpds = new ComboPooledDataSource();
            cpds.setDriverClass(p.getProperty("driverClass"));
            cpds.setJdbcUrl(p.getProperty("jdbcUrl"));
            cpds.setUser(p.getProperty("user"));
            cpds.setPassword(p.getProperty("password"));
            cpds.setMaxPoolSize(Integer.parseInt(p.getProperty("maxPoolSize")));
            cpds.setMinPoolSize(Integer.parseInt(p.getProperty("minPoolSize")));
            System.out.println("-----调用了构造方法------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            return null;
        }
    }

}