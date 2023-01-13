package com.erp.server.bi.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.erp.server.bi.service.CommonService;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @Classname ErpObjectHandler
 * @Description TODO
 * @Date 2022-12-08 15:25
 * @Created by yl
 */
@Component
public class ErpObjectHandler implements MetaObjectHandler {

    @Resource
    private CommonService commonService;

    //插入时的填充数据
    @Override
    public void insertFill(MetaObject metaObject) {
        String userId = commonService.getUserInfo().getUid();
        String userName = commonService.getUserInfo().getUserName();
        LocalDateTime nowDate = LocalDateTime.now();
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
        LocalDateTime nowDate = LocalDateTime.now();
        String userId = commonService.getUserInfo().getUid();
        String userName = commonService.getUserInfo().getUserName();
        this.setFieldValByName("updateTime", nowDate, metaObject);
        this.setFieldValByName("updateUserId", userId, metaObject);
        this.setFieldValByName("updateUserName", userName, metaObject);
    }

}
