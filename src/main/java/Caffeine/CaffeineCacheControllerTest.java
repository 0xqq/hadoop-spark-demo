package Caffeine;


public class CaffeineCacheControllerTest {
    public static void main(String args[]) throws Exception {
        String tmp = "monitor_test_boot" + "!!!@@@###" + "/query" + "!!!@@@###" + "3";
        //getExpressionFromMySQL("monitor_test_boot", "/query", 3);
        CaffeineCacheController.testLoading(tmp);
        System.out.println("==============第二次过去不应该去create ");
        CaffeineCacheController.testLoading(tmp);
        CaffeineCacheController.cacheAlltoLocal();
        CaffeineCacheController.getAllFromLocal();
    }

}
