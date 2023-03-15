package com.erp.server.scm.handler;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.common.core.utils.MathUtil;
import com.erp.server.scm.service.CommonService;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author yl
 * @Classname 处理 TableField  注解
 * @Description TODO
 * @Date 2022-07-06 9:22
 * @Created by yl
 */
@Component
public class ErpObjectHandler implements MetaObjectHandler {

    @Resource
    private CommonService commonService;

    //插入时的填充数据
    @Override
    public void insertFill(MetaObject metaObject) {
        Date now = new Date();
        String userId = commonService.getUserInfo().getUid();
        String userName = commonService.getUserInfo().getUserName();
        if(!BeanUtil.beanToMap(metaObject.getOriginalObject()).keySet().contains("isDeleted")){
            this.setFieldValByName("createTime", now, metaObject);
            this.setFieldValByName("updateTime", now, metaObject);

        }else {
            LocalDateTime localDateTime = LocalDateTime.now();
            this.setFieldValByName("version", MathUtil.ONE, metaObject);
            this.setFieldValByName("createTime", localDateTime, metaObject);
            this.setFieldValByName("updateTime", localDateTime, metaObject);
            this.setFieldValByName("createUserId", userId, metaObject);
            this.setFieldValByName("createUserName", userName, metaObject);
            this.setFieldValByName("updateUserId", userId, metaObject);
            this.setFieldValByName("updateUserName", userName, metaObject);
        }



    }

    //更新时的 填充数据
    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = commonService.getUserInfo().getUid();
        String userName = commonService.getUserInfo().getUserName();
        if(!BeanUtil.beanToMap(metaObject.getOriginalObject()).keySet().contains("isDeleted")){
            this.setFieldValByName("updateTime", new Date(), metaObject);
        }else {
            this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
            this.setFieldValByName("updateUserName", userName, metaObject);
            this.setFieldValByName("updateUserId", userId, metaObject);
        }
    }
}
