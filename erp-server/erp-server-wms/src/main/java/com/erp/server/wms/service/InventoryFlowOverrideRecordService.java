package com.erp.server.wms.service;
import com.erp.model.wms.entity.InventoryFlowOverrideRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.InventoryFlowOverrideRecordDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 库存流水重算时间范围记录 服务类
 * </p>
 *
 * @author cloud
 * @since 2024-08-09
 */
public interface InventoryFlowOverrideRecordService extends SuperService<InventoryFlowOverrideRecordEntity> {

    /**
     * 根据组织id分组查询最大结束时间
     * @param inventoryOrgId 库存组织id
     * @return List<InventoryFlowOverrideRecordEntity>
     */
    List<InventoryFlowOverrideRecordEntity> listMaxEndTimeGroupByOrgId(String inventoryOrgId);

    Map<String, LocalDateTime> mapMaxEndTimeGroupByOrgId(String inventoryOrgId);
}
