package cn.magicvector.common;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;

public class BigDecimalSerializer implements JsonSerializer<BigDecimal> {

    @Override
    public JsonElement serialize(BigDecimal src, Type typeOfSrc, JsonSerializationContext context) {
        // 将BigDecimal转换为非科学计数法的字符串表示
        return context.serialize(src.toPlainString());
    }

    public static void main(String[] args) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(BigDecimal.class, new BigDecimalSerializer())
                .create();

        BigDecimal bigDecimal = new BigDecimal("0.000000000001");
        String json = gson.toJson(bigDecimal);
        System.out.println(json); // 输出为字符串形式而非科学计数法
    }
}