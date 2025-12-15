package cn.magicvector.common.basic.mq;

import org.apache.rocketmq.client.producer.SendResult;

import java.util.List;
import java.util.Map;

public interface MessageQueue {
    /**
     * 发送普通消息
     * @param topic 主题
     * @param message 消息内容
     * @return 发送结果
     */
    SendResult sendMessage(String topic, String message);

    /**
     * 发送事务消息
     * @param topic 主题
     * @param message 消息内容
     * @param transactionId 事务 ID
     * @return 发送结果
     */
    SendResult sendTransactionMessage(String topic, String message, String transactionId);

    /**
     * 发送延迟消息
     * @param topic 主题
     * @param message 消息内容
     * @param delayLevel 延迟级别
     * @return 发送结果
     */
    SendResult sendDelayedMessage(String topic, String message, int delayLevel);

    /**
     * 发送带有自定义属性的消息
     * @param topic 主题
     * @param message 消息内容
     * @param headers 自定义消息头
     * @return 发送结果
     */
    SendResult sendMessageWithHeaders(String topic, String message, Map<String, String> headers);

    /**
     * 发送批量消息
     * @param topic 主题
     * @param messages 消息列表
     * @return 发送结果
     */
    SendResult sendBatchMessages(String topic, List<String> messages);


    /**
     * 发送指定Tag的消息
     * @param topic 主题
     * @param tag 标签
     * @param message 消息列表
     * @return 发送结果
     */
    public SendResult sendMessageWithTag(String topic, String tag, String message);
}
