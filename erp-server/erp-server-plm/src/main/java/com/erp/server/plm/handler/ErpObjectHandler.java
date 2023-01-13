package com.erp.server.plm.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.erp.server.plm.service.CommonService;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        Date now = new Date();
        String userId = commonService.getUserInfo().getUid();
        this.setFieldValByName("createTime", now, metaObject);
        this.setFieldValByName("updateTime", now, metaObject);
        this.setFieldValByName("createUserId", userId, metaObject);
        this.setFieldValByName("updateUserId", userId, metaObject);

    }

    //更新时的 填充数据
    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = commonService.getUserInfo().getUid();
        this.setFieldValByName("updateTime", new Date(), metaObject);
        this.setFieldValByName("updateUserId", userId, metaObject);

    }
}
