package com.magicvector.common;


import com.github.tbwork.anole.loader.Anole;
import com.github.tbwork.anole.loader.AnoleApp;
import com.github.tbwork.anole.loader.annotion.AnoleConfigLocation;
import com.magicvector.common.basic.mongomq.MongoMQService;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

@AnoleConfigLocation
public class MongoMQServiceTest {

    public static void main(String[] args) {
        AnoleApp.start();
        // 设置MongoDB连接URI
        String mongoUri = Anole.getProperty("mongo.uri");

        // 创建MongoDB客户端
        MongoClient mongoClient = MongoClients.create(mongoUri);
        MongoDatabase database = mongoClient.getDatabase("mongomq");
        MongoCollection<Document> collection = database.getCollection("messages");


        MongoMQService instance1 = getMongoMQ(mongoUri, collection);

        MongoMQService instance2 = getMongoMQ(mongoUri, collection);

        MongoMQService instance3 = getMongoMQ(mongoUri, collection);

        // 发送测试消息
        instance1.sendMsg("testTopic", "Hello, World!", 5000, payload -> {
            System.out.println("【实例1】Received message: " + payload);
        });

        instance2.sendMsg("testTopic", "Hello, World!", 1000000, payload -> {
            System.out.println("【实例2】Received message: " + payload);
        });

        instance3.sendMsg("testTopic", "Hello, World!", 1000000, payload -> {
            System.out.println("【实例3】Received message: " + payload);
        });


        // 等待一段时间以便处理消息
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 关闭MongoDB客户端
        mongoClient.close();
    }


    private static MongoMQService getMongoMQ(String uri,  MongoCollection<Document> collection){
        // 创建MongoMQService实例
        MongoMQService mongoMQService = new MongoMQService();
        mongoMQService.setMongoUri(uri);
        mongoMQService.setCollection(collection);

        // 初始化服务
        mongoMQService.doInit();
        return mongoMQService;
    }






}