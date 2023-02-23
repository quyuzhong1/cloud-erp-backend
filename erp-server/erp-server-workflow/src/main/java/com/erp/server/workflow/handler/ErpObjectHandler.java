package com.erp.server.workflow.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Classname 处理 TableField  注解
 * @Description TODO
 * @Date 2022-07-06 9:22
 * @Created by yl
 */
@Component
public class ErpObjectHandler implements MetaObjectHandler {

    //插入时的填充数据
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }

    //更新时的 填充数据
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
}
