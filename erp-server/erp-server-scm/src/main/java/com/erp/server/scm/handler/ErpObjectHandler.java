package com.erp.server.scm.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.common.core.utils.MathUtil;
import com.erp.server.scm.service.CommonService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

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

    /**
     *   插入时的填充数据
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        String userId = commonService.getUserInfo().getUid();
        String userName = commonService.getUserInfo().getUserName();
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
        String userId = commonService.getUserInfo().getUid();
        String userName = commonService.getUserInfo().getUserName();
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        LocalDateTime localDateTime = LocalDateTime.now();
        this.setFieldValByName("updateTime", localDateTime, metaObject);
        if (StringUtils.isNotBlank(userId)) {
            this.setFieldValByName("updateUserId", userId, metaObject);
        }
        if (StringUtils.isNotBlank(userName)) {
            this.setFieldValByName("updateUserName", userName, metaObject);
        }
    }
}
