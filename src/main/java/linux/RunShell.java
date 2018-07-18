package linux;

public class RunShell {
    public static void main(String[] args) {
        permission();

        String className = "org.apache.spark.examples.SparkPi";
        int numExecutors = 1;
        String driverMemory = "1g";
        String executorMemory = "1g";
        int executorCores = 1;
        String hdfsPath = "hdfs://nameservice1:8020/user/zouzhanshun/application/spark-examples-1.6.0-cdh5.9.3-hadoop2.6.0-cdh5.9.3.jar";
        String parameters[] = {"11", "22"};

        run(className, numExecutors, driverMemory, executorMemory, executorCores, hdfsPath, parameters);
    }

    public static void permission() {
        try {
            Runtime.getRuntime().exec("chmod 777 /home/zouzhanshun/testRunShell.sh");
        } catch (Exception e) {
            System.out.println("e: " + e);
        }
    }

    public static int run(String className,
                          int numExecutors,
                          String driverMemory,
                          String executorMemory,
                          int executorCores,
                          String hdfsPath,
                          String... parameters) {
        StringBuffer sb = new StringBuffer("nohup ");
        int result = -1;
        Process process = null;
        sb.append("/opt/apps/cloudera/parcels/SPARK2-2.2.0.cloudera2-1.cdh5.12.0.p0.232957/lib/spark2/bin/spark-submit ");
        sb.append(className + " ");
        sb.append("--num-executors " + numExecutors + " ");
        sb.append("--driver-memory " + driverMemory + " ");
        sb.append("--executor-memory " + executorMemory + " ");
        sb.append("--executor-cores " + executorCores + " ");
        sb.append("--master yarn ");
        sb.append("--deploy-mode cluster ");
        sb.append("--supervise ");
        sb.append(hdfsPath + " ");
        for (String parm : parameters) {
            if (parm != null && parm != "") {
                sb.append(parm + "");
            }
        }
        sb.append("> /tmp/logs/hadoop/" + System.nanoTime() + ".log 2>&1 &");

        try {
            process = Runtime.getRuntime().exec(sb.toString());
            process.waitFor();
            result = process.exitValue();
            if (result == 0) {
                System.out.println("=================submit job success result: " + result);
            }
        } catch (Exception e) {
            System.out.println("=================submit job failed result: " + result);
            System.out.println("e: " + e);
        } finally {
            process.destroy();
        }
        return result;
    }
}
