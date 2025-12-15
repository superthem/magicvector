package cn.magicvector.common.basic.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base6462Util {

    // Base62 字符集
    private static final char[] BASE62_DIGITS = "56789abcdefghijklmnopqrstuvwxyz01234ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int BASE62 = BASE62_DIGITS.length;
    private static final int BASE62_FAST_SIZE = 'z';
    private static final int[] BASE62_DIGITS_INDEX = new int[BASE62_FAST_SIZE + 1];

    static {
        for (int i = 0; i < BASE62_FAST_SIZE; i++) {
            BASE62_DIGITS_INDEX[i] = -1;
        }
        for (int i = 0; i < BASE62; i++) {
            BASE62_DIGITS_INDEX[BASE62_DIGITS[i]] = i;
        }
    }

    // 私有构造函数，防止实例化
    private Base6462Util() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    // ================== Base64 编码/解码 ===================

    /**
     * 使用 Base64 对字符串进行编码
     *
     * @param input 要编码的字符串
     * @return Base64 编码后的字符串
     */
    public static String encodeBase64(String input) {
        byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes(StandardCharsets.UTF_8));
        return new String(encodedBytes, StandardCharsets.UTF_8);
    }

    /**
     * 使用 Base64 对字符串进行解码
     *
     * @param encoded Base64 编码的字符串
     * @return 解码后的原始字符串
     */
    public static String decodeBase64(String encoded) {
        byte[] decodedBytes = Base64.getDecoder().decode(encoded);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    // ================== Base62 编码/解码 ===================

    /**
     * 使用 Base62 对字符串进行编码
     *
     * @param input 要编码的字符串
     * @return Base62 编码后的字符串
     */
    public static String encodeBase62(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(encodeBase62Byte(b));
        }
        return builder.toString();
    }

    /**
     * 使用 Base62 对字符串进行解码
     *
     * @param encoded Base62 编码的字符串
     * @return 解码后的原始字符串
     */
    public static String decodeBase62(String encoded) {
        StringBuilder decodedBuilder = new StringBuilder();
        for (int i = 0; i < encoded.length(); i += 2) {
            String chunk = encoded.substring(i, i + 2);
            decodedBuilder.append((char) decodeBase62Chunk(chunk));
        }
        return decodedBuilder.toString();
    }

    // ================== Base62 工具方法 ===================

    /**
     * 将 byte 编码为 Base62 字符串
     *
     * @param b 要编码的 byte
     * @return Base62 编码的字符串
     */
    private static String encodeBase62Byte(byte b) {
        int num = b & 0xFF; // 将 byte 转为无符号整数
        StringBuilder builder = new StringBuilder();
        while (num != 0) {
            builder.append(BASE62_DIGITS[num % BASE62]);
            num /= BASE62;
        }
        return builder.reverse().toString();
    }

    /**
     * 将 Base62 编码的字符串解码为 byte
     *
     * @param chunk Base62 编码的字符串
     * @return 解码后的 byte 值
     */
    private static int decodeBase62Chunk(String chunk) {
        long result = 0L;
        long multiplier = 1;
        for (int pos = chunk.length() - 1; pos >= 0; pos--) {
            char c = chunk.charAt(pos);
            if (c > BASE62_FAST_SIZE) {
                throw new IllegalArgumentException("Unknown character for Base62: " + chunk);
            }
            int index = BASE62_DIGITS_INDEX[c];
            if (index == -1) {
                throw new IllegalArgumentException("Unknown character for Base62: " + chunk);
            }
            result += index * multiplier;
            multiplier *= BASE62;
        }
        return (int) result;
    }

    public static void main(String[] args) {
        // 测试 Base64 编码/解码
        String base64Encoded = Base6462Util.encodeBase64("Hello, Base64!");
        System.out.println("Base64 Encoded: " + base64Encoded);
        String base64Decoded = Base6462Util.decodeBase64(base64Encoded);
        System.out.println("Base64 Decoded: " + base64Decoded);

        // 测试 Base62 编码/解码
        String base62Encoded = Base6462Util.encodeBase62("Hello, Base62!");
        System.out.println("Base62 Encoded: " + base62Encoded);
        String base62Decoded = Base6462Util.decodeBase62(base62Encoded);
        System.out.println("Base62 Decoded: " + base62Decoded);
    }
}