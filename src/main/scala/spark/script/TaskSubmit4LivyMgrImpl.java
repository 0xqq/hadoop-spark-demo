package spark.script;

/**
 * 向yarn 上提交Spark 任务
 */
public class TaskSubmit4LivyMgrImpl {

    //private static Logger log = Logger.getLogger("com.TaskSubmit4LivyMgrImpl");
    public static void main(String[] args) {
        String className = "org.apache.spark.examples.SparkPi";
        String name = "MY HDFS Scala Livy Pi Example";
        //Driver的内存通常来说不设置，或者设置1G左右应该就够了。唯一需要注意的一点是，如果需要使用collect算子将RDD的数据全部拉取到Driver上进行处理，那么必须确保Driver的内存足够大，否则会出现OOM内存溢出的问题。
        String driverMemory = "512m";
        //int driverCores = 6;
        //Executor进程的内存设置4G~8G   num-executors乘以executor-memory不要超过资源队列最大总内存的1/3~1/2
        String executorMemory = "1000m";
        //core数量设置为2~4 那么num-executors乘以executor-cores不要超过队列总CPU core的1/3~1/2左右比较合适
        int executorCores = 1;
        //一般设置50~100个左右的Executor进程   num-executors * executor-cores不要超过队列总CPU core的1/3~1/2左右比较合适
        int numExecutors = 2;

        String hdfsPath = "hdfs://nameservice1:8020/user/zouzhanshun/application/spark-examples-1.6.0-cdh5.9.3-hadoop2.6.0-cdh5.9.3.jar";
        String queue = "default";//default
        String parameters[] = {"10"};

        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("\"file\":").append("\"" + hdfsPath + "\"").append(",");
        sb.append("\"className\":").append("\"" + className + "\"").append(",");
        sb.append("\"name\":").append("\"" + name + "\"").append(", ");
        sb.append("\"executorCores\":").append(executorCores).append(",");
        sb.append("\"executorMemory\":").append("\"" + executorMemory + "\"").append(",");
        //sb.append("\"driverCores\":").append(driverCores).append(",");
        sb.append("\"driverMemory\":").append("\"" + driverMemory + "\"").append(",");
        sb.append("\"numExecutors\":").append(numExecutors).append(",");
        sb.append("\"queue\":").append("\"" + queue + "\"").append(",");
        if (parameters.length > 0) {
            sb.append("\"args\":");
            sb.append("[\"");
            for (String param : parameters) {
                if (param != "" && param != null) {
                    sb.append(param);
                }
            }
            sb.append("\"]");
        }
        sb.append("}");

        String url = "http://jy2tehdp01:8998/batches";
        System.out.println("============sumbit json parameters==========>" + sb.toString());
        String result = ReqEngine.sendPostReq(url, sb.toString());
        System.out.println("===============result===========>" + result);
    }
}
