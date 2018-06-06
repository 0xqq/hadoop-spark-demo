package Caffeine;


import com.alibaba.druid.pool.DruidPooledConnection;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import druid.javaBean.ServiceUriCfg;
import druid.util.DBPoolConnection;
import druid.util.DruidUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CaffeineCacheController {

    public static Cache<String, Object> manualCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build(key -> createExpensiveGraph(key));
    public static LoadingCache<String, Object> loadingCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(key -> createExpensiveGraph(key));

    public static Object createExpensiveGraph(String key) {
        String result = null;
        String[] split = key.split("!!!@@@###", -1);
        if (split.length == 3) {
            String service_code = split[0];
            String uri = split[1];
            Integer kpi_id = Integer.parseInt(split[2]);
            result = getExpressionFromMySQL(service_code, uri, kpi_id);
            System.out.println("缓存不存在或过期，调用了createExpensiveGraph方法构建新的key的值，并缓存。");
        }
        return result;
    }

    public static String getExpressionFromMySQL(String service_code, String uri, Integer kpi_id) {
        String result = null;
        DBPoolConnection dbp = DBPoolConnection.getInstance();
        DruidPooledConnection con = null;
        PreparedStatement ps = null;
        try {
            con = dbp.getConnection();
            String timeout_time = "p.timeout_time";
            String table = "sm_cfg_threshold t";
            String inner_join = "sm_cfg_service_uri p";
            String on = "t.service_code = p.service_code AND t.uri = p.uri";
            String where = "t.service_code = ? AND t.uri = ? AND t.kpi_id = ? AND p.timeout_time != 0";
            String sql = "SELECT " + timeout_time + " FROM " + table + " inner JOIN " + inner_join + " on " + on + " WHERE " + where;
            //String sql = "SELECT t.service_code,t.uri,p.timeout_time FROM sm_cfg_threshold t INNER JOIN sm_cfg_service_uri p ON t.service_code = p.service_code and t.uri = p.uri where t.kpi_id = 3 and p.timeout_time != 0";
            ps = con.prepareStatement(sql);
            ps.setString(1, service_code);
            ps.setString(2, uri);
            ps.setInt(3, kpi_id);
            System.out.println("sql: " + sql);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                result = resultSet.getString("timeout_time");
                System.out.println(result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (ps != null) ps.close();
            } catch (SQLException se2) {
            }
            try {
                if (con != null) con.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            dbp = null;

        }
        return result;
    }


    public static Object testLoading(String key) {

        // 采用同步方式去获取一个缓存和上面的手动方式是一个原理。在build Cache的时候会提供一个createExpensiveGraph函数。
        // 查询并在缺失的情况下使用同步的方式来构建一个缓存
        //Object graph = manualCache.get(key);
        loadingCache.get(key);
        System.out.println("从缓存中获取了key: " + key + " value: " + loadingCache.get(key));
        // 获取组key的值返回一个Map
        List<String> keys = new ArrayList<>();
        keys.add(key);
        //Map<String, Object> graphs = manualCache.getAll(keys);//获取转化为df
        loadingCache.getAll(keys);
        return "";
    }

    /**
     * 把数据库的全部缓存到本地缓存
     */
    public static void cacheAlltoLocal() {
        //DBUtils在创建QueryRunner时传入dataSource对象每次在执行完之后都会自动关闭Connection连接对象~所以再也不用担心没有关闭对象而导致的问题了
        //获取链接
        QueryRunner runner = new QueryRunner(DruidUtils.getDatasource());

        //编写sql语句
        final String service_code = "t.service_code,";
        final String uri = "t.uri,";
        final String kpi_id = " t.kpi_id,";
        final String timeout_time = " p.timeout_time";
        final String table = " sm_cfg_threshold t";
        final String inner_join = "sm_cfg_service_uri p";
        final String on = "t.service_code = p.service_code AND t.uri = p.uri";
        final String where = "t.kpi_id = 3 AND p.timeout_time != 0";
        final String sql = "SELECT " + service_code + uri + kpi_id + timeout_time + " FROM " + table + " INNER JOIN " + inner_join + " on " + on + " WHERE " + where;

        //返回查询值
        List<ServiceUriCfg> serviceUriCfgList = null;
        try {
            serviceUriCfgList = runner.query(sql, new BeanListHandler<>(ServiceUriCfg.class));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        loadingCache.put("serviceUriCfgList", serviceUriCfgList);
        System.out.println("==================>初始化每个url的超时时间的缓存完成：" + serviceUriCfgList);

    }

    /**
     * 查询本地缓存所有数据
     */
    public static void getAllFromLocal() {

        //loadingCache.get(key);
        //System.out.println("从缓存中获取了key: " + key + " value: " + loadingCache.get(key));
        //获取组key的值返回一个Map
        List<String> keys = new ArrayList<>();
        keys.add("monitor_test_boot!!!@@@###/query!!!@@@###3");
        keys.add("monitor-test-mvc!!!@@@###/mvc/test/testMonitor!!!@@@###3");
        keys.add("monitor-test-mvc!!!@@@###/mvc/test/t@@###3");
        Map<String, Object> all = loadingCache.getAll(keys);

        for (String key : all.keySet()) {
            System.out.println("key: " + key + " : " + all.get(key));
        }


    }


    //方法一  接受需要查询的key 返回这些key组成list ，把list转换成表 关联

    public static ArrayList<ServiceUriCfg> getListBykey(String key) {
        Object o = loadingCache.get(key);
        return (ArrayList<ServiceUriCfg>) o;
    }

}