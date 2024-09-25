package com.magicvector.common.basic.oss;

import com.alibaba.fastjson.JSONObject;
import com.magicvector.common.basic.model.OssTempKey;

public interface OssService {

    /**
     * 上传文件到OSS
     * @param bucketName 存储桶名称
     * @param fileKey 文件的唯一标识
     * @param filePath 本地文件路径
     * @return 文件的URL
     */
    String uploadFile(String bucketName, String fileKey, String filePath);

    /**
     * 从OSS下载文件
     * @param bucketName 存储桶名称
     * @param fileKey 文件的唯一标识
     * @param downloadPath 下载文件保存的路径
     */
    void downloadFile(String bucketName, String fileKey, String downloadPath);

    /**
     * 删除OSS上的文件
     * @param bucketName 存储桶名称
     * @param fileKey 文件的唯一标识
     */
    void deleteFile(String bucketName, String fileKey);

    /**
     * 生成文件的签名 URL 以供临时访问
     * @param bucketName 存储桶名称
     * @param fileKey 文件的唯一标识
     * @param expirationInSeconds URL 的有效时间（以秒为单位）
     * @return 签名 URL
     */
    String getTempReadUrl(String bucketName, String fileKey, long expirationInSeconds);


    OssTempKey getTempPrivateWriteKey(String bucketName);
}
