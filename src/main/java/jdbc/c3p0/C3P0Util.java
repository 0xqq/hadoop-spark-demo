package jdbc.c3p0;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class C3P0Util {
    //private static ComboPooledDataSource cpds = new ComboPooledDataSource();//读default-config
    private static ComboPooledDataSource cpds = new ComboPooledDataSource("service_monitor");

    public static Connection getConnection() throws SQLException {
        return cpds.getConnection();

    }

    public static DataSource getDataSource() {
        return cpds;
    }
}
