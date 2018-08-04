package kafka;

import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.Map;
import scala.collection.Set;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class KafkaTestMain {
    public static void main(String[] args) throws IOException {
        ConsoleApi consoleApi = new ConsoleApi();

        System.out.println("=====================所有topic===========================");
        List<String> topicList = consoleApi.getTopicList();
        for (String str : topicList) {
            System.out.println("所有topic: " + str);
        }
        System.out.println("=====================所有消费者组===========================");
        List<String> consumerGroups = consoleApi.getConsumerGroups();
        for (String str : consumerGroups) {
            System.out.println("所有消费者组: " + str);
        }
        System.out.println("=====================获取排序的BrokerList===========================");
        List<Object> sortedBrokerList = consoleApi.getSortedBrokerList();
        for (Object str : sortedBrokerList) {
            System.out.println("获取排序的BrokerList: " + str);
        }
        System.out.println("=====================获取消费某个topic发送消息的消费组===========================");
        for (String str : topicList) {
            //printSet(str);
        }
        //获取所有topic的配置信息
        Map<String, Properties> map = consoleApi.fetchAllTopicConfigs();
        Iterator<Tuple2<String, Properties>> iterator = map.iterator();
        while (iterator.hasNext()) {
            Tuple2<String, Properties> next = iterator.next();
            System.out.println(next._1 + "|" + next._2);
        }
    }

    public static void printSet(String topic) {
        System.out.println("=====================获取topic" + topic + "===========================");
        ConsoleApi consoleApi = new ConsoleApi();
        Set<String> allConsumerGroupsForTopic = consoleApi.getAllConsumerGroupsForTopic(topic);
        Iterator<String> it = allConsumerGroupsForTopic.iterator();
        while (it.hasNext()) {
            String str = it.next();
            System.out.println(str);
        }
    }
}
