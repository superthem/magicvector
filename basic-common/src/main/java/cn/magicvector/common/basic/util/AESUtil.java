package cn.magicvector.common.basic.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;


public class AESUtil {

    // AES 加密算法
    private static final String ALGORITHM = "AES";

    /**
     * 使用指定的密钥对字符串进行 AES 加密
     *
     * @param content 加密内容
     * @param key     密钥
     * @return 加密后的字符串 (Base64 编码)
     */
    public static String encrypt(String content, String key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(key)); // 使用 SecretKeySpec 初始化 Cipher
            byte[] encryptedBytes = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes); // 使用 Base64 编码
        } catch (Exception e) {
            throw new RuntimeException("AES 加密失败", e);
        }
    }

    /**
     * 使用指定的密钥对加密字符串进行 AES 解密
     *
     * @param encryptedContent 加密的 Base64 字符串
     * @param key              密钥
     * @return 解密后的字符串
     */
    public static String decrypt(String encryptedContent, String key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, generateKey(key)); // 使用 SecretKeySpec 初始化 Cipher
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedContent); // Base64 解码
            byte[] originalBytes = cipher.doFinal(encryptedBytes);
            return new String(originalBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES 解密失败", e);
        }
    }

    /**
     * 生成 AES 密钥
     *
     * @param key 原始密钥字符串
     * @return SecretKeySpec AES 密钥对象
     */
    private static SecretKeySpec generateKey(String key) {
        try {
            // 使用 SHA-256 生成 32 字节的密钥
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);
            keyBytes = Arrays.copyOf(keyBytes, 16); // 使用 AES-128，因此只取前16字节
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("生成 AES 密钥失败", e);
        }
    }

    public static void main(String[] args) {
        String secretKey = "my_secret_key_123"; // 密钥
        String originalText = "Hello, AES encryption!"; // 原始文本

        // 加密
        String encryptedText = AESUtil.encrypt(originalText, secretKey);
        System.out.println("加密后的内容: " + encryptedText);

        // 解密
        String decryptedText = AESUtil.decrypt(encryptedText, secretKey);
        System.out.println("解密后的内容: " + decryptedText);
    }
}
