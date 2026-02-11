package com.xiao.rocketmq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

public class ProducerExample {
    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("xiao_group");
        producer.setNamesrvAddr("172.25.78.181:9876"); // 使用你的服务器IP和端口
        producer.start();
        for (int i = 0; i < 10; i++) {
            Message msg = new Message("TopicTest", "TagA", ("Hello RocketMQ " + i).getBytes());
            producer.send(msg);
        }
        producer.shutdown();
    }
}