package com.magicvector.common.basic.mongomq;
import com.github.tbwork.anole.loader.Anole;
import com.mongodb.client.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Data
@Slf4j
public class MongoMQService {
    @Value("${magic.vector.mongomq.mongodb.uri}")
    private String mongoUri;

    private MongoCollection<Document> collection;
    private final Map<String, ICallback> topicCallbacks = new ConcurrentHashMap<>();
    private final String instanceId = UUID.randomUUID().toString();
    private volatile boolean running = true;

    @PostConstruct
    public void init() {

        if(!Anole.getBoolProperty("magic.vector.mongomq.enabled", false)){
            return;
        }

    }

    public void doInit(){
        // 初始化MongoDB连接
        MongoClient mongoClient = MongoClients.create(mongoUri);
        MongoDatabase database = mongoClient.getDatabase("mongomq");
        collection = database.getCollection("messages");

        // 创建索引
        collection.createIndex(new Document("topic", 1));
        collection.createIndex(new Document("triggerTime", 1));
        collection.createIndex(new Document("createTime", 1));
        collection.createIndex(new Document("processed", 1));

        // 启动消息处理线程
        startMessageProcessor();

        // 启动过期消息清理线程
        startMessageCleaner();
    }

    public void addCallback(String topic, ICallback callback){
        topicCallbacks.put(topic, callback);
    }
    public void sendMsg(String topic, Object msg, long delayTime, ICallback callback) {
        // 注册回调
        topicCallbacks.put(topic, callback);

        // 创建消息文档
        Document doc = new Document()
                .append("topic", topic)
                .append("payload", msg)
                .append("triggerTime", System.currentTimeMillis() + delayTime)
                .append("createTime", System.currentTimeMillis())
                .append("processed", false)
                .append("processingInstance", null)
                .append("processStartTime", 0L);

        collection.insertOne(doc);
    }

    private void startMessageProcessor() {
        Thread processor = new Thread(() -> {
            while (running) {
                try {
                    processMessages();
                    int interval = Anole.getIntProperty("magic.vector.mongomq.refresh.interval", 5);
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    log.error("MongoMQ 延时消息处理 发生错误: ", e);
                }
            }
        });
        processor.setDaemon(true);
        processor.start();
    }

    private void processMessages() {
        long now = System.currentTimeMillis();

        // 查找需要处理的消息
        for (String topic : topicCallbacks.keySet()) {
            FindIterable<Document> messages = collection.find(
                    new Document("topic", topic)
                            .append("processed", false)
                            .append("triggerTime", new Document("$lte", now))
            );

            for (Document message : messages) {
                // 尝试锁定消息
                Document query = new Document("_id", message.getObjectId("_id"))
                        .append("processed", false)
                        .append("processingInstance", null);

                Document update = new Document("$set", new Document()
                        .append("processingInstance", instanceId)
                        .append("processStartTime", now));

                Document result = collection.findOneAndUpdate(query, update);

                if (result != null) {
                    try {
                        // 执行回调
                        ICallback callback = topicCallbacks.get(topic);
                        callback.onMessage(message.get("payload"));

                        // 标记为已处理
                        collection.updateOne(
                                new Document("_id", message.getObjectId("_id")),
                                new Document("$set", new Document("processed", true))
                        );
                    } catch (Exception e) {
                        // 处理失败，释放锁
                        collection.updateOne(
                                new Document("_id", message.getObjectId("_id")),
                                new Document("$set", new Document()
                                        .append("processingInstance", null)
                                        .append("processStartTime", 0L))
                        );
                    }
                }
            }
        }
    }

    private void startMessageCleaner() {
        Thread cleaner = new Thread(() -> {
            while (running) {
                try {
                    // 删除一个月前的消息
                    long monthAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;
                    collection.deleteMany(
                            new Document("createTime", new Document("$lt", monthAgo))
                    );
                    Thread.sleep(24 * 60 * 60 * 1000); // 每天运行一次
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleaner.setDaemon(true);
        cleaner.start();
    }
}