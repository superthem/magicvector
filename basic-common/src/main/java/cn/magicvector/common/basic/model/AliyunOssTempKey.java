package cn.magicvector.common.basic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.*;

@Data
@AllArgsConstructor
public class AliyunOssTempKey implements OssTempKey{
    private final String providerName = "alibaba";
    private String accessKeyId;
    private String accessKeySecret;
    private String securityToken;
    private Date expireAt;
}
