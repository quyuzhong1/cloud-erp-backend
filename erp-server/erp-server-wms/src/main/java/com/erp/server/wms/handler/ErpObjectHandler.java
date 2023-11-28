package com.erp.server.wms.handler;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.common.business.vo.LoginUser;
import com.common.core.utils.MathUtil;
import com.erp.server.wms.service.CommonService;
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

    //插入时的填充数据
    @Override
    public void insertFill(MetaObject metaObject) {
        LoginUser userInfo = commonService.getUserInfo();
        String uid = userInfo.getUid();
        String userNameStr = userInfo.getUserName();
        String userId = StrUtil.isBlank(uid) ? "0": uid;
        String userName = StrUtil.isBlank(userNameStr) ? "system": userNameStr;
        LocalDateTime localDateTime = LocalDateTime.now();
        this.setFieldValByName("version", MathUtil.ONE, metaObject);
        this.setFieldValByName("createTime", localDateTime, metaObject);
        this.setFieldValByName("updateTime", localDateTime, metaObject);
        this.fillStrategy(metaObject,"createUserId", userId);
        this.fillStrategy( metaObject, "updateUserId", userId);
        this.fillStrategy( metaObject, "createUserName", userName);
        this.fillStrategy( metaObject, "updateUserName", userName);

    }

    //更新时的 填充数据
    @Override
    public void updateFill(MetaObject metaObject) {
        LoginUser userInfo = commonService.getUserInfo();
        String uid = userInfo.getUid();
        String userNameStr = userInfo.getUserName();
        String userId = StrUtil.isBlank(uid) ? "0": uid;
        String userName = StrUtil.isBlank(userNameStr) ? "system": userNameStr;
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateUserName", userName, metaObject);
        this.setFieldValByName("updateUserId", userId, metaObject);
    }
}
