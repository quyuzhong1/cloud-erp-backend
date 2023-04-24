package com.erp.server.workflow.handler;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.erp.server.workflow.service.CommonService;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Classname 处理 TableField  注解
 * @Description TODO
 * @Date 2022-07-06 9:22
 * @Created by yl
 */
@Component
public class ErpObjectHandler implements MetaObjectHandler {

    @Autowired
    private CommonService commonService;

    //插入时的填充数据
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String userId = commonService.getUserInfo().getUid();
        String userName = commonService.getUserInfo().getUserName();
        this.setFieldValByName("createUserId", userId, metaObject);
        this.setFieldValByName("createUserName", userName, metaObject);
        this.setFieldValByName("updateUserId", userId, metaObject);
        this.setFieldValByName("updateUserName", userName, metaObject);
        this.setFieldValByName("createTime", localDateTime, metaObject);
        this.setFieldValByName("updateTime", localDateTime, metaObject);
    }


    //更新时的 填充数据
    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = commonService.getUserInfo().getUid();
        String userName = commonService.getUserInfo().getUserName();
        this.setFieldValByName("updateUserId", userId, metaObject);
        this.setFieldValByName("updateUserName", userName, metaObject);
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
}
