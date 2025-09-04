package com.magicvector.common.application.counter.impl;

import com.magicvector.common.application.counter.CouterService;
import com.magicvector.common.basic.cache.Cache;
import com.magicvector.common.basic.util.S;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
@ConditionalOnProperty(name = "mv.redis.enabled", havingValue = "true", matchIfMissing = true)
public class CounterServiceImpl implements CouterService {

    // 内存中临时存储的计数器，基于统计组和指标名进行存储
    private final Map<String, Map<String, AtomicLong>> localCounter = new ConcurrentHashMap<>();

    // 临时存储唯一ID集合，基于统计组和指标名进行存储
    private final Map<String, Map<String, Set<String>>> localUniqueCounter = new ConcurrentHashMap<>();

    // Redis缓存模拟（假设cache是已经定义好的全局对象）
    @Autowired
    @Qualifier("baseCache")
    private Cache cache;

    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public CounterServiceImpl() {
        // 定时任务，每隔5秒同步数据到Redis
        scheduler.scheduleAtFixedRate(this::syncToRedis, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void counter(String group, String index, String uniqueId) {
        if (S.isEmpty(uniqueId)) {
            // 无唯一ID，仅做简单计数
            localCounter
                    .computeIfAbsent(group, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(index, k -> new AtomicLong(0))
                    .incrementAndGet();
        } else {
            // 有唯一ID，统计唯一ID的集合
            localUniqueCounter
                    .computeIfAbsent(group, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(index, k -> ConcurrentHashMap.newKeySet())
                    .add(uniqueId);
        }
    }

    @Override
    public Long getCounterValue(String group, String index) {
        // 先获取纯数字计数
        String cacheKey = getRedisKey(group, index);
        Long result = cache.get(cacheKey);
        if(result == null){
            return cache.sCard(cacheKey);
        }
        return result;
    }

    @Override
    public void cleanCounter(String group, String index) {
        // 清除内存中的数据
        localCounter.computeIfPresent(group, (g, indexMap) -> {
            indexMap.remove(index);
            return indexMap.isEmpty() ? null : indexMap;
        });
        localUniqueCounter.computeIfPresent(group, (g, indexMap) -> {
            indexMap.remove(index);
            return indexMap.isEmpty() ? null : indexMap;
        });

        // 清除Redis中的数据
        cache.remove(getRedisKey(group, index));
    }

    @Override
    public void cleanGroup(String group) {

        Map<String, AtomicLong> pureDigitGroup = localCounter.get(group);
        Map<String, Set<String>> uniqueIdsGroup = localUniqueCounter.get(group);

        // 清除Redis中的数据
        for (String index : pureDigitGroup.keySet()) {
            String cacheKey = getRedisKey(group,index);
            cache.remove(cacheKey);
        }

        for (String index : uniqueIdsGroup.keySet()) {
            String cacheKey = getRedisKey(group,index);
            cache.remove(cacheKey);
        }
        // 清除整个组的内存数据
        cleanLocalGroup(group);
    }

    private void cleanLocalGroup(String group){
        // 清除整个组的内存数据
        localCounter.remove(group);
        localUniqueCounter.remove(group);
    }

    @Override
    public List<String> getCounterDetail(String group, String index) {
        // 从Redis中获取set集合中的所有uniqueId
        Set<String> uniqueIds = cache.sMembers(getRedisKey(group, index));
        return uniqueIds != null ? new ArrayList<>(uniqueIds) : Collections.emptyList();
    }

    // 将内存中的数据同步到Redis
    private void syncToRedis() {
        // 同步计数
        for (String group : localCounter.keySet()) {
            Map<String, AtomicLong> indexMap = localCounter.get(group);
            for (String index : indexMap.keySet()) {
                long count = indexMap.get(index).get();
                cache.increaseBy(getRedisKey(group, index), (int) count);
            }
            // 同步完就清空本地
            cleanLocalGroup(group);
        }


        // 同步唯一ID集合
        for (String group : localUniqueCounter.keySet()) {
            Map<String, Set<String>> indexMap = localUniqueCounter.get(group);
            for (String index : indexMap.keySet()) {
                Set<String> uniqueIds = indexMap.get(index);
                if(uniqueIds.isEmpty()){
                    continue;
                }
                List<String[]> uidBatches = splitSet(uniqueIds, 20);
                for (String[] uidBatch : uidBatches) {
                    cache.sAdd(getRedisKey(group, index), uidBatch);
                }
            }
            // 同步完就清空本地
            cleanLocalGroup(group);
        }
    }

    // 生成用于存储到Redis中的键名
    private String getRedisKey(String group, String index) {
        return "COUNTER:" + group + ":" + index;
    }

    private static List<String[]> splitSet(Set<String> uniqueIds, int batchSize) {
        List<String[]> resultList = new ArrayList<>();
        String[] currentBatch = new String[batchSize];
        int index = 0;

        for (String uniqueId : uniqueIds) {
            currentBatch[index++] = uniqueId;

            // 当当前批次达到 batchSize，添加到结果列表并重置
            if (index == batchSize) {
                resultList.add(currentBatch);
                currentBatch = new String[batchSize];
                index = 0;
            }
        }

        // 如果最后一批不满 batchSize，也需要加入结果列表
        if (index > 0) {
            String[] lastBatch = new String[index];
            System.arraycopy(currentBatch, 0, lastBatch, 0, index);
            resultList.add(lastBatch);
        }

        return resultList;
    }

}