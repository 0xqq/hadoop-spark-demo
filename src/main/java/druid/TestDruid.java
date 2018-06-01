package druid;

import com.alibaba.druid.pool.DruidPooledConnection;
import druid.javaBean.ServiceUriCfg;
import druid.util.DruidUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * hadoop文件的读写
 */
//TODO hadoop文件的读写
public class TestDruid {
    public static void main(String args[]) throws Exception {
        String sql = "SELECT t.service_code,t.uri,t.timeout_time FROM sm_cfg_service_uri t";
        new TestDruid().executeQueryBySQL(sql);

    }


    public void executeQueryBySQL(String sql) {
        QueryRunner runner = new QueryRunner(DruidUtils.getDatasource());
        //返回查询值
        List<ServiceUriCfg> serviceUriCfgList = null;
        try {
            serviceUriCfgList = runner.query(sql, new BeanListHandler<>(ServiceUriCfg.class));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(serviceUriCfgList);

        /*
        DBPoolConnection dbp = DBPoolConnection.getInstance();
        DruidPooledConnection con = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            con = dbp.getConnection();
            ps = con.prepareStatement(sql);
            resultSet = ps.executeQuery();
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBPoolConnection.release(resultSet, ps, con);
            dbp = null;
        }
        /*
        DBPoolConnection dbp = DBPoolConnection.getInstance();
        DruidPooledConnection con = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;

        List<ServiceUriCfg> sucfgList = new ArrayList<ServiceUriCfg>();
        while (resultSet.next()) {
            con = dbp.getConnection();
            ps = con.prepareStatement(sql);
            resultSet = ps.executeQuery();
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
        DBPoolConnection.release(resultSet,ps,con);
        ps.close();
        con.close();
        dbp = null;
    }
    */
    }
}
