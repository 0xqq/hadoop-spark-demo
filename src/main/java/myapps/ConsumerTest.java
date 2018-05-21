package myapps;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerTest {
    public static void main(String[] args) throws IOException {
        //加载配置参数
        String path = Thread.currentThread().getContextClassLoader().getResource("application.properties").getPath();
        Properties p = new Properties();
        InputStream is = new FileInputStream(new File(path));
        p.load(is);

        //整理入参
        Properties props = new Properties();
        props.put("bootstrap.servers", p.get("brokers"));
        props.put("group.id", p.get("groupId"));
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "latest");// 从头开始 earliest 上线修改为从结尾开始 latest
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        String  topics = (String) p.get("topics");
        consumer.subscribe(Arrays.asList(topics));
        //消费topics
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records)
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
        }
    }
}