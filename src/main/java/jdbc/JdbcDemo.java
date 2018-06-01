package jdbc;

import druid.javaBean.ServiceUriCfg;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JdbcDemo {

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            //获取连接的方式一
            //conn = DriverManager.getConnection("jdbc:mysql://172.18.101.118:3306/service_monitor?user=service_monitor&password=9RqG^6#N@ltY8xRz&characterEncoding=utf8");
            //获取连接的方式二
            //conn = DriverManager.getConnection("jdbc:mysql://172.18.101.118:3306/service_monitor", "service_monitor", "9RqG^6#N@ltY8xRz");
            //获取连接的方式三 建议
            Properties info = new Properties();
            info.setProperty("user", "service_monitor");
            info.setProperty("password", "9RqG^6#N@ltY8xRz");
            conn = DriverManager.getConnection("jdbc:mysql://172.18.101.118:3306/service_monitor", info);
            stmt = conn.createStatement();
            //执行查询
            ResultSet resultSet = stmt.executeQuery("SELECT t.service_code,t.uri,t.timeout_time FROM sm_cfg_service_uri t");
            //有结果集返回true,无结果集false
            //boolean b = stmt.execute("select * from sm_cfg_service_uri");
            //返回影响结果的条数
            //int i = stmt.executeUpdate("update sm_cfg_service_uri set uri='/add'");
            List<ServiceUriCfg> sucfgList = new ArrayList<ServiceUriCfg>();

            while (resultSet.next()) {
                //获取指定列的值
                String service_code = resultSet.getString("service_code");
                String uri = resultSet.getString("uri");
                Integer timeout_time = resultSet.getInt("timeout_time");
                //按照索引获取，从1开始
                //String service_code = resultSet.getString(1);
                //String uri = resultSet.getString(2);
                //Integer timeout_time = resultSet.getInt(3);
                sucfgList.add(new ServiceUriCfg(service_code, uri, timeout_time));

            }

            for (ServiceUriCfg serviceUriCfg : sucfgList) {
                System.out.println("serviceUriCfg.toString(): " + serviceUriCfg.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭连接方式一
            //release1(rs, stmt, conn);
            //关闭连接方式二
            release2(rs, stmt, conn);
        }
    }

    /**
     * 关闭连接
     *
     * @param rs
     * @param stmt
     * @param conn
     */
    public static void release2(ResultSet rs, Statement stmt, Connection conn) {
        //关闭连接方式一
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        if (conn != null) {
                            try {
                                conn.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 关闭连接
     *
     * @param rs
     * @param stmt
     * @param conn
     */
    public static void release1(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            rs = null;
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            stmt = null;
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            conn = null;
        }
    }
}
