package cn.magicvector.common.basic.util;


import com.github.tbwork.anole.loader.Anole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * RSA非对称加密，对于长度超出117的字符串做了分段加密处理
 *
 * @author zhangg
 */
@Slf4j
public class RSAUtil {


    public static String publicKey = Anole.getProperty("rsa.key.public");
    public static String privateKey = Anole.getProperty("rsa.key.private");

    /**
     * RSA公钥加密
     *
     * @param value 加密字符串
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String encrypt(String value) {
        try {
            return encrypt(value, publicKey);
        } catch (Exception ex) {
            log.info("encrypt error info:{}", ex.getMessage());
        }
        return "";
    }


    /**
     * RSA公钥加密
     *
     * @param value     加密字符串
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String encrypt(String value, String publicKey) throws Exception {
        //base64编码的公钥
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] inputArray = value.getBytes("UTF-8");
        int inputLength = inputArray.length;

        // 最大加密字节数，超出最大字节数需要分组加密
        int MAX_ENCRYPT_BLOCK = 117;
        // 标识
        int offSet = 0;
        byte[] resultBytes = {};
        byte[] cache = {};
        while (inputLength - offSet > 0) {
            if (inputLength - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(inputArray, offSet, MAX_ENCRYPT_BLOCK);
                offSet += MAX_ENCRYPT_BLOCK;
            } else {
                cache = cipher.doFinal(inputArray, offSet, inputLength - offSet);
                offSet = inputLength;
            }
            resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
            System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
        }
        String outStr = Base64.encodeBase64String(resultBytes);
        return outStr;
    }

    /**
     * RSA私钥解密
     *
     * @param value 加密字符串
     * @return 铭文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String value) {
        try {
            return decrypt(value, privateKey);
        } catch (Exception ex) {
            log.info("decrypt error info:{}", ex.getMessage());
        }
        return "";
    }

    /**
     * RSA私钥解密
     *
     * @param value      加密字符串
     * @param privateKey 私钥
     * @return 铭文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String value, String privateKey) throws Exception {
        //base64编码的私钥
        byte[] decoded = Base64.decodeBase64(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);

        byte[] inputArray = Base64.decodeBase64(value.getBytes("UTF-8"));
        int inputLength = inputArray.length;

        // 最大加密字节数，超出最大字节数需要分组加密
        int MAX_ENCRYPT_BLOCK = 128;
        // 标识
        int offSet = 0;
        byte[] resultBytes = {};
        byte[] cache = {};
        while (inputLength - offSet > 0) {
            if (inputLength - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(inputArray, offSet, MAX_ENCRYPT_BLOCK);
                offSet += MAX_ENCRYPT_BLOCK;
            } else {
                cache = cipher.doFinal(inputArray, offSet, inputLength - offSet);
                offSet = inputLength;
            }
            resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
            System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
        }
        String outStr = new String(resultBytes);
        return outStr;
    }

}

