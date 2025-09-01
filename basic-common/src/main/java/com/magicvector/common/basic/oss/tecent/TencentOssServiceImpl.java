package com.magicvector.common.basic.oss.tecent;

import com.github.tbwork.anole.loader.Anole;
import com.magicvector.common.basic.model.OssTempKey;
import com.magicvector.common.basic.model.TencentOssTempKey;
import com.magicvector.common.basic.oss.OssService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.DeleteObjectRequest;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@Slf4j
public class TencentOssServiceImpl implements OssService {

    private COSClient cosClient;

    private String secretId;

    private String secretKey;


    private String region;


    public TencentOssServiceImpl() {
        secretId = Anole.getProperty("mv.oss.tencent.secret.id");
        secretKey = Anole.getProperty("mv.oss.tencent.secret.key");
        region = Anole.getProperty("mv.oss.tencent.region");
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        cosClient = new COSClient(credentials, clientConfig);
    }

    @Override
    public String uploadFile(String bucketName, String fileKey, String filePath) {
        File file = new File(filePath);
        PutObjectResult result = cosClient.putObject(bucketName, fileKey, file);
        long expireTime = Anole.getLongProperty("mv.oss.read.expire.time", 3600);
        return generatePresignedUrl(bucketName, fileKey, expireTime); // 默认有效期为1小时
    }

    @Override
    public void downloadFile(String bucketName, String fileKey, String downloadPath) {
        try {
            File file = new File(downloadPath);
            cosClient.getObject(new GetObjectRequest(bucketName, fileKey), file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFile(String bucketName, String fileKey) {
        cosClient.deleteObject(new DeleteObjectRequest(bucketName, fileKey));
    }

    @Override
    public String getTempReadUrl(String bucketName, String fileKey, long expirationInSeconds) {
        Date expiration = new Date(System.currentTimeMillis() + expirationInSeconds * 1000);
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, fileKey, HttpMethodName.GET);
        generatePresignedUrlRequest.setExpiration(expiration);
        URL url = cosClient.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    @Override
    public OssTempKey getTempPrivateWriteKey(String bucketName) {

        TreeMap<String, Object> config = new TreeMap<String, Object>();
        config.put("secretId", secretId);
        config.put("secretKey", secretKey);
        long expiration = Anole.getLongProperty("anole.oss.write.token.expire.time", 1800);
        config.put("durationSeconds", expiration);
        config.put("bucket", bucketName);
        config.put("region", region);
        config.put("allowPrefixes", new String[]{
                "*"
        });
        String[] allowActions = new String[]{
                // 简单上传
                "name/cos:PutObject",
                "name/cos:PostObject",
                // 分片上传
                "name/cos:InitiateMultipartUpload",
                "name/cos:ListMultipartUploads",
                "name/cos:ListParts",
                "name/cos:UploadPart",
                "name/cos:CompleteMultipartUpload"
        };
        config.put("allowActions", allowActions);
        try {
            Response response = CosStsClient.getCredential(config);

            TencentOssTempKey ossTempKey = new TencentOssTempKey();
            ossTempKey.setAccessKeyId(response.credentials.tmpSecretId);
            ossTempKey.setSecretAccessKey(response.credentials.tmpSecretKey);
            ossTempKey.setSessionToken(response.credentials.sessionToken);
            ossTempKey.setToken(response.credentials.token);
            ossTempKey.setExpireAt(new Date(response.expiredTime));

            return ossTempKey;
        } catch (IOException e) {
            log.error("获取腾讯云名为{}的bucket的写入临时密钥时失败, 具体原因：{}", e.getMessage(), e);
            return null;
        }


    }

    private String generatePresignedUrl(String bucketName, String fileKey, long expirationInSeconds) {
        Date expiration = new Date(System.currentTimeMillis() + expirationInSeconds * 1000);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileKey)
                .withMethod(HttpMethodName.GET)
                .withExpiration(expiration);
        URL url = cosClient.generatePresignedUrl(request);
        return url.toString();
    }
}
