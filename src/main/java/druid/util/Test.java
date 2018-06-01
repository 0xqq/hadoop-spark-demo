package druid.util;

import druid.javaBean.ServiceUriCfg;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.util.List;

public class Test {
    @org.junit.Test
    public void query() {
        //DBUtils在创建QueryRunner时传入dataSource对象每次在执行完之后都会自动关闭Connection连接对象~所以再也不用担心没有关闭对象而导致的问题了
        //获取链接
        QueryRunner runner = new QueryRunner(DruidUtils.getDatasource());

        //编写sql语句
        String sql = "SELECT service_code,uri,timeout_time FROM sm_cfg_service_uri";
        //BeanListHandler beanListHandler = new BeanListHandler();

        //返回查询值
        List<ServiceUriCfg> serviceUriCfgList = null;
        try {
            serviceUriCfgList = runner.query(sql, new BeanListHandler<>(ServiceUriCfg.class));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(serviceUriCfgList);

    }
}
