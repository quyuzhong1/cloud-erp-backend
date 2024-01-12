package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.entity.SysUserWechatEntity;

/**
 * <p>
 * 微信用户表 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
public interface SysUserWechatService extends SuperService<SysUserWechatEntity> {
    SysUserWechatEntity getWxInfo(String uid);
}
