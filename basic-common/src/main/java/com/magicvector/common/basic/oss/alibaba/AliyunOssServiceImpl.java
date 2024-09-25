package com.magicvector.common.basic.oss.alibaba;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.github.tbwork.anole.loader.Anole;
import com.magicvector.common.basic.model.AliyunOssTempKey;
import com.magicvector.common.basic.model.OssTempKey;
import com.magicvector.common.basic.oss.OssService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.*;

@Slf4j
public class AliyunOssServiceImpl implements OssService {

    AliyunOssServiceImpl(){
        endpoint = Anole.getProperty("magic.vector.oss.aliyun.endpoint");
        accessKeyId = Anole.getProperty("magic.vector.oss.aliyun.accessKey");
        accessKeySecret = Anole.getProperty("magic.vector.oss.aliyun.accessSecret");
        region = Anole.getProperty("magic.vector.oss.aliyun.region");
        roleArn = Anole.getProperty("magic.vector.oss.aliyun.role.arn");
    }

    private String endpoint ;
    private String accessKeyId;
    private String accessKeySecret;
    private String region; // 地域
    private String roleArn;

    // 创建 OSSClient 实例
    private OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

    @Override
    public String uploadFile(String bucketName, String fileKey, String filePath) {
        try {
            // 使用 OSS SDK 上传文件
            ossClient.putObject(bucketName, fileKey, new File(filePath));
            // 构建文件的URL
            String fileUrl = String.format("https://%s.%s/%s", bucketName, endpoint.replace("https://", ""), fileKey);
            return fileUrl;
        } catch (Exception e) {
            log.error("阿里云上传文件失败, 错误原因：{}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void downloadFile(String bucketName, String fileKey, String downloadPath) {
        try {
            // 使用 OSS SDK 下载文件
            ossClient.getObject(new GetObjectRequest(bucketName, fileKey), new File(downloadPath));
            log.info("文件下载成功，已存放至：{}", downloadPath);
        } catch (Exception e) {
            log.error("阿里云下载文件失败, 错误原因：{}", e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String bucketName, String fileKey) {
        try {
            // 使用 OSS SDK 删除文件
            ossClient.deleteObject(bucketName, fileKey);
            log.info("文件删除成功");
        } catch (Exception e) {
            log.error("阿里云文件删除失败, 错误原因：{}", e.getMessage(), e);
        }
    }

    @Override
    public String getTempReadUrl(String bucketName, String fileKey, long expirationInSeconds) {
        try {
            // 设置 URL 过期时间
            Date expiration = new Date(System.currentTimeMillis() + expirationInSeconds * 1000);

            // 生成签名 URL
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileKey, HttpMethod.GET);
            request.setExpiration(expiration);

            URL signedUrl = ossClient.generatePresignedUrl(request);
            return signedUrl.toString();
        } catch (Exception e) {
            log.error("阿里云OSS生成签名链接失败, 错误原因：{}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public OssTempKey getTempPrivateWriteKey(String bucketName) {
        try {
            // 初始化客户端
            DefaultProfile profile = DefaultProfile.getProfile(region, accessKeyId, accessKeySecret);
            IAcsClient client = new DefaultAcsClient(profile);

            // 创建 AssumeRole 请求
            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setSysMethod(MethodType.POST);
            request.setRoleArn(roleArn);
            request.setRoleSessionName("upload-session");

            long startTime = new Date().getTime();
            long expiration = Anole.getLongProperty("anole.oss.write.token.expire.time", 1800);
            request.setDurationSeconds(expiration); // 临时凭证的有效时间，单位秒

            // 获取临时凭证
            AssumeRoleResponse response = client.getAcsResponse(request);
            String securityToken = response.getCredentials().getSecurityToken();
            String tempAccessKeyId = response.getCredentials().getAccessKeyId();
            String tempAccessKeySecret = response.getCredentials().getAccessKeySecret();
            Date expireAt = new Date( startTime + expiration);
            return new AliyunOssTempKey(tempAccessKeyId,tempAccessKeySecret,securityToken, expireAt);
        } catch (Exception e) {
            log.error("获取私有Bucket的临时访问令牌出错！报错信息：{}", e.getMessage(), e);
            return null;
        }
    }

    // 释放资源
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}