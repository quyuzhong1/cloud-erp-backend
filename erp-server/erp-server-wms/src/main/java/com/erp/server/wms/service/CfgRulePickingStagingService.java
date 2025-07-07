package com.erp.server.wms.service;

import com.erp.model.wms.entity.CfgRulePickingStagingEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 拣货暂存规则 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-06-07
 */
public interface CfgRulePickingStagingService extends SuperService<CfgRulePickingStagingEntity> {

    CfgRulePickingStagingEntity getByWarehouseId(String warehouseId, String billType);
}
