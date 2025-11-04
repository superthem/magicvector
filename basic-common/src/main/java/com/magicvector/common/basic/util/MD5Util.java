package com.magicvector.common.basic.util;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 工具类
 * 提供字符串的 MD5 哈希值计算功能
 */
public class MD5Util {

    /**
     * 将输入的字符串转换为 MD5 哈希值（32位小写十六进制字符串）
     *
     * @param orgStr 待加密的原始字符串
     * @return MD5 哈希值的 32 位小写十六进制字符串，如果输入为 null 则返回 null
     */
    public static String getMd5(String orgStr) {
        // 处理空值
        if (orgStr == null) {
            return null;
        }

        try {
            // 获取 MD5 消息摘要实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 将输入字符串转换为字节数组并更新摘要
            md.update(orgStr.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            // 执行哈希计算，得到字节数组形式的摘要
            byte[] digest = md.digest();

            // 将字节数组转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                // 将每个字节转换为两位十六进制数（不足两位前面补0），并转换为小写
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            // 理论上不会发生，因为 MD5 是标准算法
            throw new RuntimeException("MD5 算法不可用", e);
        }
    }

    // ------------------------ 可选：其他常用方法 ------------------------

    /**
     * （可选）获取大写格式的 MD5 值
     */
    public static String getMd5UpperCase(String orgStr) {
        String md5 = getMd5(orgStr);
        return md5 != null ? md5.toUpperCase() : null;
    }

    /**
     * （可选）获取 16 位 MD5 值（取 32 位中间 16 位）
     */
    public static String getMd5_16(String orgStr) {
        String md5 = getMd5(orgStr);
        if (md5 != null && md5.length() == 32) {
            return md5.substring(8, 24); // 取第 8 到 23 位（共 16 位）
        }
        return null;
    }

}