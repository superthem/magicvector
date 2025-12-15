package cn.magicvector.common.basic.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author dengxg
 * @date 2020/11/4 6:38 下午
 */
public class UUIDUtil {

    private final static String FORMATTER = "yyyyMMddHHmmss";

    public static String getUUID() {
        String uuidStr =getCurrentUUID();
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMATTER);
        String dateStr = date.format(formatter);
        return dateStr.concat(uuidStr);
    }

    public static String getAccountUUID(){
       return getCurrentUUID();
    }

    private static String getCurrentUUID(){
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString().replaceAll("\\-", "");
        return uuidStr;
    }

}
