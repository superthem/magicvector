package com.magicvector.common.application.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class TokenGenerator {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final int RANDOM_PART_LENGTH = 32; // 32 characters from CHARACTERS
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateToken() {
        // 获取当前时间戳（毫秒）
        long timestamp = System.currentTimeMillis();
        // 生成随机部分
        StringBuilder randomPart = new StringBuilder(RANDOM_PART_LENGTH);
        for (int i = 0; i < RANDOM_PART_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            randomPart.append(CHARACTERS.charAt(index));
        }
        // 组合时间戳和随机字符串
        String rawToken = timestamp + randomPart.toString();
        return hashToken(rawToken); // 直接返回64位字符的十六进制字符串
    }

    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            // 将哈希结果转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0'); // 补零
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static void main(String[] args) {
        String token = generateToken();
        System.out.println("Generated Unique Token: " + token);
    }
}