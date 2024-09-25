package com.magicvector.common.basic.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhangg
 */
public class ListUtil {

    /**
     * 交集
     */
    public static <T> List<T> intersection(List<? extends T> list1, List<? extends T> list2) {
        final List<? extends T> localList1 = list1;
        final List<? extends T> localList2 = list2;
        return localList1.stream().filter(item -> localList2.contains(item)).collect(Collectors.toList());
    }

    /**
     * 差集
     */
    public static <T> List<T> difference(List<? extends T> list1, List<? extends T> list2) {
        final List<? extends T> localList1 = list1;
        final List<? extends T> localList2 = list2;
        return localList1.stream().filter(item -> !localList2.contains(item)).collect(Collectors.toList());
    }

    /**
     * 并集
     */
    public static <E> List<E> union(List<? extends E> list1, List<? extends E> list2) {
        List<E> result = new ArrayList(list1.size() + list2.size());
        result.addAll(list1);
        result.addAll(list2);
        return result;
    }

    /**
     * 去重并集
     */
    public static <E> List<E> distinctUnion(List<? extends E> list1, List<? extends E> list2) {
        List<E> result = new ArrayList(list1.size() + list2.size());
        result.addAll(list1);
        result.addAll(list2);
        return result.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 组合拼接
     */
    public static List<String> combinationSplicing(List<String> list1, List<String> list2) {
        List<String> resultList = list1.stream().flatMap(str -> list2.stream().map(str.concat(":")::concat))
                .collect(Collectors.toList());
        return resultList;
    }

    /**
     * 组合拼接 1-2 equal 2-1
     */
    public static List<String> distinctCombinationSplicing(List<String> list1, List<String> list2) {
        List<String> resultList = list1.stream().flatMap(str -> list2.stream().filter(item -> item.compareToIgnoreCase(str) > 0).map(str.concat(":")::concat))
                .collect(Collectors.toList());
        return resultList;
    }

    /**
     * 提取出集合中重复的值
     */
    public static <T> List<T> findDuplicateElements(List<T> list) {
        return list.stream()
                .collect(Collectors.toMap(e -> e, e -> 1, (a, b) -> a + b))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
    }

}
