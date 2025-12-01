package com.erp.server.fms.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.MathUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author wuht
 * @Classname 处理 TableField  注解
 * @Date 2025-10-20
 * @Created by wuht
 */
@Component
public class ErpObjectHandler implements MetaObjectHandler {

    //插入时的填充数据
    @Override
    public void insertFill(MetaObject metaObject) {
        LoginUser loginUser = UserContext.getNonLoginUser();
        String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
        LocalDateTime localDateTime = LocalDateTime.now();
        this.setFieldValByName("version", MathUtil.ONE, metaObject);
        this.fillStrategy(metaObject,"createTime", localDateTime);
        this.fillStrategy(metaObject,"updateTime", localDateTime);
        this.fillStrategy(metaObject,"createUserId", userId);
        this.fillStrategy( metaObject, "updateUserId", userId);
        this.fillStrategy( metaObject, "createUserName", userName);
        this.fillStrategy( metaObject, "updateUserName", userName);
    }

    //更新时的 填充数据
    @Override
    public void updateFill(MetaObject metaObject) {
        LoginUser loginUser = UserContext.getNonLoginUser();
        String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateUserName", userName, metaObject);
        this.setFieldValByName("updateUserId", userId, metaObject);
    }
}

