package com.erp.server.srm.handler;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.common.core.utils.MathUtil;
import com.erp.server.srm.service.CommonService;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author yl
 * @Classname 处理 TableField  注解

 * @Date 2022-07-06 9:22
 * @Created by yl
 */
@Component
public class ErpObjectHandler implements MetaObjectHandler {

    @Resource
    private CommonService commonService;

    /**
     * 插入时的填充数据
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        String userId = StrUtil.isBlank(commonService.getUserInfo().getUid()) ? "0": commonService.getUserInfo().getUid();
        String userName = StrUtil.isBlank(commonService.getUserInfo().getUserName()) ? "system": commonService.getUserInfo().getUserName();
        LocalDateTime localDateTime = LocalDateTime.now();
        this.setFieldValByName("version", MathUtil.ONE, metaObject);
        this.setFieldValByName("createTime", localDateTime, metaObject);
        this.setFieldValByName("updateTime", localDateTime, metaObject);
        this.setFieldValByName("createUserId", userId, metaObject);
        this.setFieldValByName("createUserName", userName, metaObject);
        this.setFieldValByName("updateUserId", userId, metaObject);
        this.setFieldValByName("updateUserName", userName, metaObject);
    }


    /**
     * 更新时的 填充数据
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = StrUtil.isBlank(commonService.getUserInfo().getUid()) ? "0": commonService.getUserInfo().getUid();
        String userName = StrUtil.isBlank(commonService.getUserInfo().getUserName()) ? "system": commonService.getUserInfo().getUserName();
        LocalDateTime localDateTime = LocalDateTime.now();
        this.setFieldValByName("updateTime", localDateTime, metaObject);
        this.setFieldValByName("updateUserId", userId, metaObject);
        this.setFieldValByName("updateUserName", userName, metaObject);
    }
}
