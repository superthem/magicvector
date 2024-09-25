package com.magicvector.common.basic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TencentOssTempKey implements OssTempKey{
    private String providerName = "tencent";
    private String accessKeyId;
    private String secretAccessKey;
    private String sessionToken;
    private String token;
    private Date expireAt;
}
