package com.erp.server.dmp.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.common.business.constant.UserStateConstants;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.MetaUtil;
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
        Object createTime = null;;
		try {
			createTime = metaObject.getValue("createTime");
		} catch (Exception e) {
		}
        if(createTime == null) {
        	this.setFieldValByName("createTime", nowDate, metaObject);
        }
        Object updateTime = null;
		try {
			updateTime = metaObject.getValue("updateTime");
		} catch (Exception e) {
		}
        if(updateTime == null) {
        	this.setFieldValByName("updateTime", nowDate, metaObject);
        }
        Boolean isUserSystem = MetaUtil.getIsUserSystem(metaObject);
        if (Boolean.TRUE.equals(isUserSystem)) {
            // 使用系统用户
            userId = UserStateConstants.USER_SYSTEM_ID;
            userName = UserStateConstants.USER_SYSTEM;
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
