package com.magicvector.common.basic.mq.impl;

import com.magicvector.common.basic.mq.MessageQueue;
import com.magicvector.common.basic.util.Asserts;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MessageQueueImpl implements MessageQueue {

    private DefaultMQProducer producer;
    private TransactionMQProducer transactionProducer;

    @Value("${rocketmq.producer.group:null}")
    private String producerGroup;

    @Value("${rocketmq.name.server.address:null}")
    private String nameServerAddress;

    private volatile boolean banned = false;

    @PostConstruct
    public void init() {
        if(producerGroup.equals("null")||nameServerAddress.equals("null")){
            log.warn("未设置rocketmq.producer.group和rocketmq.name.server.address，MQ功能将被禁用。");
            banned = true;
            return;
        }
        producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(nameServerAddress);
        transactionProducer = new TransactionMQProducer(producerGroup);
        transactionProducer.setNamesrvAddr(nameServerAddress);
        try{
            producer.start();
            transactionProducer.start();
        }
        catch (Exception e){
            log.error("RocketMQ生产者启动出错，具体原因：{}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (producer != null) {
            producer.shutdown();
        }
        if (transactionProducer != null) {
            transactionProducer.shutdown();
        }
    }

    @Override
    public SendResult sendMessage(String topic, String message) {
        try {
            preCheck();
            Message msg = new Message(topic, message.getBytes());
            return producer.send(msg);
        } catch (Exception e) {
            log.error("RocketMQ生产者消息发送出错，具体原因：{}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public SendResult sendTransactionMessage(String topic, String message, String transactionId) {
        try {
            preCheck();
            Message msg = new Message(topic, message.getBytes());
            return transactionProducer.sendMessageInTransaction(msg, transactionId);
        } catch (Exception e) {
            log.error("RocketMQ生产者消息发送出错，具体原因：{}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public SendResult sendDelayedMessage(String topic, String message, int delayLevel) {
        try {
            preCheck();
            Message msg = new Message(topic, message.getBytes());
            msg.setDelayTimeLevel(delayLevel);
            return producer.send(msg);
        } catch (Exception e) {
            log.error("RocketMQ生产者消息发送出错，具体原因：{}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public SendResult sendMessageWithHeaders(String topic, String message, Map<String, String> headers) {
        try {
            preCheck();
            Message msg = new Message(topic, message.getBytes());
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                msg.putUserProperty(entry.getKey(), entry.getValue());
            }
            return producer.send(msg);
        } catch (Exception e) {
            log.error("RocketMQ生产者消息发送出错，具体原因：{}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public SendResult sendBatchMessages(String topic, List<String> messages) {
        try {
            preCheck();
            List<Message> msgList = new ArrayList<>();
            for (String message : messages) {
                msgList.add(new Message(topic, message.getBytes()));
            }
            return producer.send(msgList);
        } catch (Exception e) {
            log.error("RocketMQ生产者消息发送出错，具体原因：{}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public SendResult sendMessageWithTag(String topic, String tag, String message) {
        try {
            preCheck();
            Message msg = new Message(topic, tag, message.getBytes());
            return producer.send(msg);
        } catch (Exception e) {
            log.error("发送指定标签的消息失败，具体原因：{}", e.getMessage(), e);
            return null;
        }
    }

    private void preCheck(){
        Asserts.assertTrue(!banned, "MQ已被禁用，无法使用。如需启用，请设置rocketmq.producer.group和rocketmq.name.server.address。");
    }
}
