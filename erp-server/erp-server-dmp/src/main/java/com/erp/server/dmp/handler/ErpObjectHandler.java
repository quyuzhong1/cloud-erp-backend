package com.erp.server.dmp.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Classname ErpObjectHandler

 * @Date 2022-12-08 15:25
 * @Created by yl
 */
@Component
public class ErpObjectHandler implements MetaObjectHandler {

    //插入时的填充数据
    @Override
    public void insertFill(MetaObject metaObject) {
        LoginUser loginUser = UserContext.getNonLoginUser();
        String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
        LocalDateTime nowDate = LocalDateTime.now();
        Object createTime = metaObject.getValue("createTime");
        if(createTime == null) {
        	this.setFieldValByName("createTime", nowDate, metaObject);
        }
        Object updateTime = metaObject.getValue("updateTime");
        if(updateTime == null) {
        	this.setFieldValByName("updateTime", nowDate, metaObject);
        }
        this.setFieldValByName("createUserId", userId, metaObject);
        this.setFieldValByName("createUserName", userName, metaObject);
        this.setFieldValByName("updateUserId", userId, metaObject);
        this.setFieldValByName("updateUserName", userName, metaObject);
    }

    //更新时的 填充数据
    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime nowDate = LocalDateTime.now();
        LoginUser loginUser = UserContext.getNonLoginUser();
        String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
        this.setFieldValByName("updateTime", nowDate, metaObject);
        this.setFieldValByName("updateUserId", userId, metaObject);
        this.setFieldValByName("updateUserName", userName, metaObject);
    }

}
