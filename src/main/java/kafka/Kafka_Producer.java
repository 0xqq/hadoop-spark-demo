package kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Kafka_Producer {
    public static void main(String[] args) throws IOException {

        //加载配置参数
        String path = Thread.currentThread().getContextClassLoader().getResource("application.properties").getPath();
        Properties p = new Properties();
        InputStream is = new FileInputStream(new File(path));
        p.load(is);

        Properties prop = new Properties();
        prop.put("bootstrap.servers", p.get("brokers"));
        prop.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        prop.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<>(prop);
        for (int i = 0; i < 100; i++) {
            String key = "data_increment_data.kafka.service_monitor.source.user.*.*.*";
            String value = "{\"id\": 1，\"name\": \"test\"，\"phone\":\"18074546423\"，\"address\": \"Beijing\"，\"time\": \"2017-12-22 10:00:00\"}";
            producer.send(new ProducerRecord<String, String>((String) p.get("topics"), key, value));
            System.out.println("send: " + Integer.toString(i) + ":" + Integer.toString(i));
        }
        producer.close();
    }
}