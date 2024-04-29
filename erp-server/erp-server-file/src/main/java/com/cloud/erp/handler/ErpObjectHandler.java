package com.cloud.erp.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.cloud.erp.context.UserService;
import com.common.business.vo.LoginUser;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Component
public class ErpObjectHandler implements MetaObjectHandler {
    @Resource
    private UserService userService;

    //插入时的填充数据
    @Override
    public void insertFill(MetaObject metaObject) {
        LoginUser userInfo = userService.getCurrentUser();
        LocalDateTime nowDate = LocalDateTime.now();
        this.setFieldValByName("createTime", nowDate, metaObject);
        this.setFieldValByName("updateTime", nowDate, metaObject);
        this.setFieldValByName("createUserId", userInfo.getUid(), metaObject);
        this.setFieldValByName("createUserName", userInfo.getUserName(), metaObject);
        this.setFieldValByName("updateUserId", userInfo.getUid(), metaObject);
        this.setFieldValByName("updateUserName", userInfo.getUserName(), metaObject);
    }

    //更新时的 填充数据
    @Override
    public void updateFill(MetaObject metaObject) {
        LoginUser userInfo = userService.getCurrentUser();
        LocalDateTime nowDate = LocalDateTime.now();
        this.setFieldValByName("updateTime", nowDate, metaObject);
        this.setFieldValByName("updateUserId", userInfo.getUid(), metaObject);
        this.setFieldValByName("updateUserName", userInfo.getUserName(), metaObject);
    }

}
