package com.magicvector.common;

import com.google.gson.*;
import com.magicvector.common.rest.adapter.DateTypeAdapterFactory;
import com.magicvector.common.rest.config.SpringfoxJsonToGsonAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import springfox.documentation.spring.web.json.Json;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
public class GsonTest {



    public static class Request<T>{
        T param;
    }

    @Data
    @AllArgsConstructor
    public static class Person {
        String name;
        int age;

        BigDecimal risk;
    }


    // 自定义BigDecimal序列化器

    // 自定义BigDecimal序列化器
    public static class BigDecimalSerializer implements JsonSerializer<BigDecimal> {
        @Override
        public JsonElement serialize(BigDecimal src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.stripTrailingZeros()); // 保留为数字类型
        }
    }
    public static void main(String[] args) {
        GsonBuilder builder = new GsonBuilder()
                .serializeSpecialFloatingPointValues()  // 处理特殊浮点值
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .serializeNulls()
                // 启用BigDecimal以禁用科学计数法
                .disableHtmlEscaping()    // 可选：禁用HTML转义，保留原始字符
                .registerTypeAdapterFactory(new DateTypeAdapterFactory())
                .registerTypeAdapter(Json.class, new SpringfoxJsonToGsonAdapter())
                .registerTypeAdapter(BigDecimal.class, new BigDecimalSerializer()); // 注册自定义序列化器
        Gson gson = builder.create();


        Request<String> a = new Request<>();

        Person person = new Person("123", 123,
                BigDecimal.valueOf(1).divide(new BigDecimal("1000000000000000"))
                );

        System.out.println(gson.toJson(person));
    }
}
