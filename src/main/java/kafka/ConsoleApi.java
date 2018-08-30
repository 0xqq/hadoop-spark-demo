package kafka;

import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.security.JaasUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import scala.collection.JavaConversions;
import scala.collection.Map;
import scala.collection.Set;

import java.io.IOException;
import java.util.*;

public class ConsoleApi {
    /*@Autowired
    private KafkaConsumerConfig consumerConfig;
    private ZkUtils zkUtils = null;
    private ZooKeeper zooKeeper = null;

    *//**
     * -获取集群信息（与getAllBrokersInCluster（）只是返回类型不一致）
     *//*
    public Cluster getCluster() {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        return zkUtils.getCluster();
    }

    public void getLeaderAndIsrForPartition(String topicName, int patition) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        System.out.println("打印：" + zkUtils.getLeaderAndIsrForPartition(topicName, patition));
    }

    public boolean createConsumer(String groupId, String topic) {
        try {
            Properties properties = new Properties();
            properties.put("zookeeper.connect", consumerConfig.getZookeeperConn());//声明zk
            properties.put("group.id", groupId);
            ConsumerConnector consumer = new ZookeeperConsumerConnector(new ConsumerConfig(properties), true);
            java.util.Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
            if (topic != null && !"".equals(topic)) {
                topicCountMap.put(topic, 1); // 一次从主题中获取一个数据
            } else {
                topicCountMap.put("topic", 1); // 一次从主题中获取一个数据
            }
            java.util.Map<String, List<KafkaStream<byte[], byte[]>>> messageStreams = consumer.createMessageStreams(topicCountMap);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean createConsumer(String groupId) {
        return createConsumer(groupId, null);
    }

    public boolean deleteUselessConsumer(String group) {
        return deleteUselessConsumer("-1", group);
    }

    *//**
     * -删除topic路径
     *
     * @return
     *//*
    public String deleteTopicsPath() {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        return zkUtils.DeleteTopicsPath();
    }

    *//**
     * -根据brokerId获取broker的信息
     *
     * @param brokerId
     *//*
    public Broker getBrokerInfo(int brokerId) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        return zkUtils.getBrokerInfo(brokerId).get();
    }

    *//**
     * -获取消费者的路径
     *
     * @return
     *//*

    public String ConsumersPath() {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        return zkUtils.ConsumersPath();
    }

    *//**
     * 删除多个topic
     *
     * @param topicNames
     * @return
     *//*
    public String[] deleteTopics(final String... topicNames) {
        if (topicNames == null || topicNames.length == 0) return new String[0];
        java.util.Set<String> deleted = new LinkedHashSet<String>();
        for (String topicName : topicNames) {
            if (topicName != null || !topicName.trim().isEmpty()) {
                deleteTopic(topicName);
                deleted.add(topicName.trim());
            }
        }
        return deleted.toArray(new String[deleted.size()]);
    }

    *//**
     * 获取所有的TopicList
     *
     * @return
     *//*
    public List<String> getTopicList() {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        List<String> allTopicList = JavaConversions.seqAsJavaList(zkUtils.getAllTopics());
        return allTopicList;
    }

    *//**
     * ~获取某个分组下的所有消费者
     *
     * @param groupName
     * @return
     *//*
    public List<String> getConsumersInGroup(String groupName) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        List<String> allTopicList = JavaConversions.seqAsJavaList(zkUtils.getConsumersInGroup(groupName));
        return allTopicList;
    }

    *//**
     * 判断某个topic是否存在
     *
     * @param topicName
     * @return
     *//*
    public boolean topicExists(String topicName) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        boolean exists = AdminUtils.topicExists(zkUtils, topicName);
        return exists;
    }

    *//**
     * ~
     *
     * @param groupName
     * @return
     *//*
    public boolean isConsumerGroupActive(String groupName) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        boolean exists = AdminUtils.isConsumerGroupActive(zkUtils, groupName);
        return exists;
    }

    *//**
     * 获取所有消费者组
     *
     * @return
     *//*
    public List<String> getConsumerGroups() {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        List<String> set = JavaConversions.seqAsJavaList(zkUtils.getConsumerGroups());
        return set;
    }

    *//**
     * 根据消费者的名称获取topic
     *
     * @param groupName
     * @return
     *//*
    public List<String> getTopicsByConsumerGroup(String groupName) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        List<String> set2 = JavaConversions.seqAsJavaList(zkUtils.getTopicsByConsumerGroup(groupName));
        return set2;
    }

    *//**
     * 获取排序的BrokerList
     *
     * @return
     *//*
    public List<Object> getSortedBrokerList() {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        List<Object> set2 = JavaConversions.seqAsJavaList(zkUtils.getSortedBrokerList());
        return set2;
    }

    public List<Broker> getAllBrokersInCluster() {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        List<Broker> set2 = JavaConversions.seqAsJavaList(zkUtils.getAllBrokersInCluster());
        return set2;
    }

    *//**
     * 获取消费某个topic发送消息的消费组
     *
     * @param topicName
     * @return
     *//*
    public Set<String> getAllConsumerGroupsForTopic(String topicName) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        Set<String> stringSeq = zkUtils.getAllConsumerGroupsForTopic(topicName);
        return stringSeq;
    }

    *//**
     * 获取删除主题的路径
     *
     * @param topicName
     * @return
     *//*
    public String getDeleteTopicPath(String topicName) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        String stringSeq = zkUtils.getDeleteTopicPath(topicName);
        return stringSeq;
    }

    *//**
     * 获取topic路径
     *
     * @param topicName
     * @return
     *//*
    public String getTopicPath(String topicName) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        String stringSeq = zkUtils.getTopicPath(topicName);
        return stringSeq;
    }

    public boolean createTopic(String topicName) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        try {
            //
            AdminUtils.createTopic(zkUtils, topicName, 1, 1, new Properties(), RackAwareMode.Enforced$.MODULE$);
            return true;
        } catch (RuntimeException e) {

        }
        return false;
    }

    *//**
     * 删除topic信息（前提是server.properties中要配置delete.topic.enable=true）
     *
     * @param topicName
     *//*
    public void deleteTopic(String topicName) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        // 删除topic 't1'
        AdminUtils.deleteTopic(zkUtils, topicName);
        System.out.println("删除成功！");
    }

    *//**
     * 删除topic的某个分区
     *
     * @param brokerId
     * @param topicName
     *//*
    public void deletePartition(int brokerId, String topicName) {
        // 删除topic 't1'
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        zkUtils.deletePartition(brokerId, topicName);
        System.out.println("删除成功！");
    }


    *//**
     * 改变topic的配置
     *
     * @param topicName
     *//*
    public void updateTopic(String topicName) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());

        Properties props = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), "test");
        // 增加topic级别属性
        props.put("min.cleanable.dirty.ratio", "0.3");
        // 删除topic级别属性
        props.remove("max.message.bytes");
        props.put("retention.ms", "1000");
        // 修改topic 'test'的属性
        AdminUtils.changeTopicConfig(zkUtils, "test", props);
        System.out.println("修改成功");
        zkUtils.close();
    }

    *//**
     * 获取所有topic的配置信息
     *//*
    public Map<String, Properties> fetchAllTopicConfigs() {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        return AdminUtils.fetchAllTopicConfigs(zkUtils);
    }

    *//**
     * 获取所有topic或者client的信息()type为：ConfigType.Topic()/ConfigType.Client()
     *//*
    public Map<String, Properties> fetchAllEntityConfigs(String type) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());
        return AdminUtils.fetchAllEntityConfigs(zkUtils, type);
    }

    *//**
     * 获取指定topic的配置信息
     *
     * @param topicName
     *//*
    public Properties fetchEntityConfig(String topicName) {
        zkUtils = ZkUtils.apply(consumerConfig.getZookeeperConn(), 30000, 30000, JaasUtils.isZkSecurityEnabled());

        return AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), topicName);
    }

    private boolean deleteUselessConsumer(String topic, String group) {
        //if (topic.endsWith("-1")) {
        StringBuilder sb = new StringBuilder().append("/consumers/")
                .append(group);
        return recursivelyDeleteData(sb.toString());
    }

    private boolean recursivelyDeleteData(String path) {
        List<String> childList = getChildrenList(path);
        if (childList == null) {
            return false;
        } else if (childList.isEmpty()) {
            deleteData(path);
        } else {
            for (String childName : childList) {
                String childPath = path + "/" + childName;
                List<String> grandChildList = getChildrenList(childPath);
                if (grandChildList == null) {
                    return false;
                } else if (grandChildList.isEmpty()) {
                    deleteData(childPath);
                } else {
                    recursivelyDeleteData(childPath);
                }
            }
            deleteData(path);
        }
        return true;
    }

    private boolean deleteData(String path) {
        try {
            zooKeeper.delete(path, -1);
        } catch (InterruptedException e) {
            //log.error("delete error,InterruptedException:" + path, e);
            return false;
        } catch (KeeperException e) {
            //log.error("delete error,KeeperException:" + path, e);
            return false;
        }
        return true;
    }

    private List<String> getChildrenList(String path) {

        try {
            zooKeeper = new ZooKeeper(consumerConfig.getZookeeperConn(), 6000, null);
            return zooKeeper.getChildren(path, false, null);
        } catch (KeeperException e) {
            return null;
        } catch (InterruptedException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<String>(Collections.singleton(path));
    }

    *//**
     * get all subscribing consumer group names for a given topic
     *
     * @param brokerListUrl localhost:9092 for instance
     * @param topic         topic name
     * @return
     *//*
    public static java.util.Set<String> getAllGroupsForTopic(String brokerListUrl, String topic) {
        AdminClient client = AdminClient.createSimplePlaintext(brokerListUrl);
        try {
            List<GroupOverview> allGroups = scala.collection.JavaConversions.seqAsJavaList(client.listAllGroupsFlattened().toSeq());
            java.util.Set<String> groups = new HashSet<String>();
            for (GroupOverview overview : allGroups) {
                String groupID = overview.groupId();
                java.util.Map<TopicPartition, Object> offsets = JavaConversions.mapAsJavaMap(client.listGroupOffsets(groupID));
                groups.add(groupID);
            }
            return groups;
        } finally {
            client.close();
        }
    }*/

}