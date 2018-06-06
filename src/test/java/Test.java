import java.sql.*;

public class Test {
    // JDBC 驱动名及数据库 URL
    //static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://172.18.101.118:3306/";

    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "service_monitor";
    static final String PASS = "9RqG^6#N@ltY8xRz";

    public static void main(String[] args) {

        String str = "123456789";
        System.out.println(str.substring(1,5).toString());




        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName("com.mysql.jdbc.Driver");

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT CONCAT(t.service_code,'_',t.kpi_id,'_',t.uri) as mykey,replace(t.threshold_expression,'COM_VAL',t.threshold_value) as myvalue from service_monitor.sm_cfg_threshold t";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while (rs.next()) {
                // 通过字段检索
                String mykey = rs.getString("mykey");
                String myvalue = rs.getString("myvalue");

                // 输出数据
                System.out.print("mykey: " + mykey);
                System.out.print("myvalue: " + myvalue);
                System.out.print("\n");
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            }// 什么都不做
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}
