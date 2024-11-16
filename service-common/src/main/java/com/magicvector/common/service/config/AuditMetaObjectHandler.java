package com.magicvector.common.service.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.magicvector.common.basic.context.GlobalContext;
import com.magicvector.common.basic.util.S;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AuditMetaObjectHandler implements MetaObjectHandler {

    private static final String createdBy = "creator";
    private static final String createdTime = "createTime";
    private static final String lastModifiedBy = "lastOperator";
    private static final String lastModifiedTime = "updateTime";

    private static final Logger log = LoggerFactory.getLogger(AuditMetaObjectHandler.class);

    public AuditMetaObjectHandler() {
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        String username = this.getExecuteUserName(metaObject, createdBy);
        boolean createdDate = metaObject.hasSetter(createdTime);
        boolean lastModifiedDate = metaObject.hasSetter(lastModifiedTime);
        if (createdDate || lastModifiedDate) {
            if (createdDate) {
                this.setFieldValByName(createdTime, this.getSetDate(metaObject, createdTime), metaObject);
            }

            if (lastModifiedDate) {
                this.setFieldValByName(lastModifiedTime, this.getSetDate(metaObject, lastModifiedTime), metaObject);
            }
        }

        if (metaObject.hasSetter(createdBy)) {
            this.setFieldValByName(createdBy, username, metaObject);
        }

        if (metaObject.hasSetter(lastModifiedBy)) {
            this.setFieldValByName(lastModifiedBy, username, metaObject);
        }


    }

    public void updateFill(MetaObject metaObject) {
        String username = this.getExecuteUserName(metaObject, lastModifiedBy);
        boolean lastModifiedDate = metaObject.hasSetter(lastModifiedTime);
        if (lastModifiedDate) {
            this.setFieldValByName(lastModifiedTime, this.getSetDate(metaObject, lastModifiedTime), metaObject);
        }

        if (metaObject.hasSetter(lastModifiedBy)) {
            this.setFieldValByName(lastModifiedBy, username, metaObject);
        }

    }

    private String getExecuteUserName(MetaObject metaObject, String fieldName) {
        String executeUserName = "SYSTEM";

        String valByName = (String) this.getFieldValByName(fieldName, metaObject);
        if (valByName != null) {
            executeUserName = valByName;
        } else {
            try {
                if(GlobalContext.getCurrentUser()!=null){
                    Map<String, Object> userProps = GlobalContext.getCurrentUser().getUserProps();
                    if(userProps != null && userProps.get("id") != null){
                        executeUserName = userProps.get("id").toString();
                    }
                }
            } catch (Exception e) {
                log.info("找不到当前线程上下文中的用户信息:{}", e.getMessage());
            }
        }
        return executeUserName;
    }

    private Date getSetDate(MetaObject metaObject, String fieldName) {
        Date setDate = (Date) this.getFieldValByName(fieldName, metaObject);
        Date executeDate;
        if (null != setDate) {
            executeDate = setDate;
        } else {
            executeDate = new Date();
        }
        return executeDate;
    }
}
