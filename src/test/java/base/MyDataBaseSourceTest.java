package base;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class MyDataBaseSourceTest {
    public static void main(String[] args) throws SQLException {
        Connection connection = MyDataBaseSource.DATASOURCE.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT service_code,uri,timeout_time FROM sm_cfg_service_uri");
        ResultSetMetaData metaData = preparedStatement.getMetaData();
        System.out.println(metaData.toString());
        //MyDataBaseSource.DATASOURCE.getConnection() ;
        //MyDataBaseSource.DATASOURCE.getConnection() ;
    }
}
