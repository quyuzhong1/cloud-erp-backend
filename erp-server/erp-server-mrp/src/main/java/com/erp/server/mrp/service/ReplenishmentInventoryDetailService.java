package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.InventoryTotalDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.entity.ReplenishmentInventoryDetailEntity;
import com.erp.model.mrp.vo.InventoryDetailVO;

import java.util.List;

/**
 * <p>
 * 库存详情 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface ReplenishmentInventoryDetailService extends SuperService<ReplenishmentInventoryDetailEntity> {

    /**
     * 根据补货建议明细id查询库存明细
     */
    List<ReplenishmentInventoryDetailEntity> listByReplenishmentDetailIds(List<String> ids);

    /**
     * 获取库存明细
     */
    InventoryDetailVO inventoryDetail(InventoryTotalDTO params);

    /**
     * 保存库存明细及店铺明细
     *
     * @param inventoryDetail       明细
     * @param replenishmentDetailId 建议明细id
     * @param calcVersion           计算版本
     */
    void saveInventoryDetail(List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> inventoryDetail, String replenishmentDetailId, String calcVersion);

}
