package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.wms.dto.CfgRulePickingStagingDTO;
import com.erp.model.wms.entity.CfgRulePickingStagingEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.WarehouseLocationEntity;

import java.util.List;
import java.util.Map;

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

    /**
     * 查看暂存仓位列表
     * @return
     */
    List<CfgRulePickingStagingDTO.StagingDTO> viewStaging();

    /**
     * 保存暂存仓位
     *
     * @param dto
     * @param warehouseName
     * @param locationMap
     * @return
     */
    BatchResultDTO saveStaging(CfgRulePickingStagingDTO.StagingDTO dto, String warehouseName, Map<String, WarehouseLocationEntity> locationMap);

    List<CfgRulePickingStagingEntity> listByWarehouseIds(List<String> strings);

    void removeOtherWarehouse(List<String> warehouseIds);
}
