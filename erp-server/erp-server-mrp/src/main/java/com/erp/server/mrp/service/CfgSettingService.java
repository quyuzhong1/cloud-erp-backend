package com.erp.server.mrp.service;

import com.erp.model.mrp.entity.CfgSettingEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 系统配置管理 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-09-03
 */
public interface CfgSettingService extends SuperService<CfgSettingEntity> {

    CfgSettingEntity getCfgSetting(String code);

}
