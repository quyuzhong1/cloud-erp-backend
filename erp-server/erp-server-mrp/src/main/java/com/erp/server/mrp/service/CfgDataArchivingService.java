package com.erp.server.mrp.service;

import com.erp.model.mrp.entity.CfgDataArchivingEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 归档配置 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-09-26
 */
public interface CfgDataArchivingService extends SuperService<CfgDataArchivingEntity> {

    List<CfgDataArchivingEntity> getEffectiveData();

    void archiveData(CfgDataArchivingEntity config);
}
