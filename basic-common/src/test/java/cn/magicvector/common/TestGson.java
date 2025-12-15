package cn.magicvector.common;
import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.*;

public class TestGson {


    @Data
    public static class TestObj{
        private Date today;
        private String greetings;
    }
    public static void main(String[] args) {

        TestObj testObj = new TestObj();
        testObj.setGreetings("hello");
        testObj.setToday(new Date());

        String jsonString = "{\"greetings\":\"hello\",\"today\":\"2025-02-26 14:39:00\"}";


        TestObj obj =  JSON.parseObject(jsonString, TestObj.class);
        System.out.println(obj.getToday());
    }
}
