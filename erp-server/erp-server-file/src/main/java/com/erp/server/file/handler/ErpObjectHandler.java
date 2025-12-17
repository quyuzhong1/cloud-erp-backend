package com.erp.server.file.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.common.business.constant.UserStateConstants;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.MetaUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ErpObjectHandler implements MetaObjectHandler {

    //插入时的填充数据
    @Override
    public void insertFill(MetaObject metaObject) {
        LoginUser userInfo = UserContext.getNonLoginUser();
        String userId = userInfo.getUid();
        String userName = userInfo.getUserName();
        LocalDateTime nowDate = LocalDateTime.now();
        Boolean isUserSystem = MetaUtil.getIsUserSystem(metaObject);
        if (Boolean.TRUE.equals(isUserSystem)) {
            // 使用系统用户
            userId = UserStateConstants.USER_SYSTEM_ID;
            userName = UserStateConstants.USER_SYSTEM;
        }
        this.setFieldValByName("createTime", nowDate, metaObject);
        this.setFieldValByName("updateTime", nowDate, metaObject);
        this.setFieldValByName("createUserId", userId, metaObject);
        this.setFieldValByName("createUserName", userName, metaObject);
        this.setFieldValByName("updateUserId", userId, metaObject);
        this.setFieldValByName("updateUserName", userName, metaObject);
    }

    //更新时的 填充数据
    @Override
    public void updateFill(MetaObject metaObject) {
        LoginUser userInfo = UserContext.getNonLoginUser();
        LocalDateTime nowDate = LocalDateTime.now();
        String userId = userInfo.getUid();
        String userName = userInfo.getUserName();
        Boolean isUserSystem = MetaUtil.getIsUserSystem(metaObject);
        if (Boolean.TRUE.equals(isUserSystem)) {
            // 使用系统用户
            userId = UserStateConstants.USER_SYSTEM_ID;
            userName = UserStateConstants.USER_SYSTEM;
        }
        this.setFieldValByName("updateTime", nowDate, metaObject);
        this.setFieldValByName("updateUserId", userId, metaObject);
        this.setFieldValByName("updateUserName", userName, metaObject);
    }

}
