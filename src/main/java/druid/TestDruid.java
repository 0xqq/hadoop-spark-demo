package druid;

import com.alibaba.druid.pool.DruidPooledConnection;
import druid.javaBean.ServiceUriCfg;
import druid.util.DBPoolConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TestDruid {
    public static void main(String args[]) throws Exception {
        String sql = "SELECT t.service_code,t.uri,t.timeout_time FROM sm_cfg_service_uri t";
        new TestDruid().executeQueryBySQL(sql);

    }

    public void executeUpdateBySQL(String sql) throws SQLException {
        DBPoolConnection dbp = DBPoolConnection.getInstance();
        DruidPooledConnection con = dbp.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.executeUpdate();
        ps.close();
        con.close();
        dbp = null;
    }

    public void executeQueryBySQL(String sql) throws SQLException {
        DBPoolConnection dbp = DBPoolConnection.getInstance();
        DruidPooledConnection con = dbp.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet resultSet = ps.executeQuery();
        List<ServiceUriCfg> sucfgList = new ArrayList<ServiceUriCfg>();
        while (resultSet.next()) {
            String service_code = resultSet.getString("service_code");
            String uri = resultSet.getString("uri");
            Integer timeout_time = resultSet.getInt("timeout_time");
            ServiceUriCfg serviceUriCfg = new ServiceUriCfg(service_code, uri, timeout_time);
            sucfgList.add(serviceUriCfg);
            //System.out.println("=====>table " + service_code + " : " + uri + " : " + timeout_time + "<=====");
        }
        for (ServiceUriCfg serviceUriCfg : sucfgList) {
            System.out.println("serviceUriCfg.toString(): " + serviceUriCfg.toString());
        }
        ps.close();
        con.close();
        dbp = null;
    }
}
