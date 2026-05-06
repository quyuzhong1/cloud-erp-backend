package com.erp.server.admin.handler;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.common.business.constant.UserStateConstants;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.MathUtil;
import com.common.core.utils.MetaUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Classname 处理 TableField  注解
 * @Date 2022-07-06 9:22
 * @Created by yl
 */
@Component
public class ErpObjectHandler implements MetaObjectHandler {

    //插入时的填充数据
    @Override
    public void insertFill(MetaObject metaObject) {
        Date now = new Date();
        LoginUser loginUser = UserContext.getNonLoginUser();
        String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
        Boolean isUserSystem = MetaUtil.getIsUserSystem(metaObject);
        if (Boolean.TRUE.equals(isUserSystem)) {
            // 使用系统用户
            userId = UserStateConstants.USER_SYSTEM_ID;
            userName = UserStateConstants.USER_SYSTEM;
        }
        if (!BeanUtil.beanToMap(metaObject.getOriginalObject()).keySet().contains("isDeleted")) {
            this.setFieldValByName("createTime", now, metaObject);
            this.setFieldValByName("updateTime", now, metaObject);
        } else {
            LocalDateTime localDateTime = LocalDateTime.now();
            this.setFieldValByName("version", MathUtil.ONE, metaObject);
            this.setFieldValByName("createTime", localDateTime, metaObject);
            this.setFieldValByName("updateTime", localDateTime, metaObject);
        }
        this.fillStrategy(metaObject, "createUserId", userId);
        this.fillStrategy(metaObject, "createUserName", userName);
        this.setFieldValByName("updateUserId", userId, metaObject);
        this.setFieldValByName("updateUserName", userName, metaObject);
    }

    //更新时的 填充数据
    @Override
    public void updateFill(MetaObject metaObject) {
        LoginUser loginUser = UserContext.getNonLoginUser();
        String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
        Boolean isUserSystem = MetaUtil.getIsUserSystem(metaObject);
        if (Boolean.TRUE.equals(isUserSystem)) {
            // 使用系统用户
            userId = UserStateConstants.USER_SYSTEM_ID;
            userName = UserStateConstants.USER_SYSTEM;
        }
        if (!BeanUtil.beanToMap(metaObject.getOriginalObject()).keySet().contains("isDeleted")) {
            this.setFieldValByName("updateTime", new Date(), metaObject);
        } else {
            this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }
        this.setFieldValByName("updateUserName", userName, metaObject);
        this.setFieldValByName("updateUserId", userId, metaObject);
    }
}
