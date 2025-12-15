package cn.magicvector.common.basic.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhangg
 * @description： 消息推送
 */
@Data
public class UserIdentityInfo implements Serializable {

    /**
     * 用户主键
     * */
    private String userId;

    /**
     * 平台
     * */
    private String platform;
    /**
     * 用户身份
     * */
    private String identity;
}
