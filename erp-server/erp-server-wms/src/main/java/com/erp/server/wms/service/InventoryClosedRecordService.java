package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.inventory.InventoryClosedRecordDTO;
import com.erp.model.wms.entity.InventoryClosedRecordEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 库存关账记录表 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-10-12
 */
public interface InventoryClosedRecordService extends SuperService<InventoryClosedRecordEntity> {

    /**
     * 批量操作
     *
     * @author Jim
     * @date: 2023-10-12
     */
    void actionBatch(List<InventoryClosedRecordEntity> newEntityList, List<InventoryClosedRecordEntity> oldEntityList);

    /**
     * 查询最新库存关账记录
     *
     * @author Jim
     * @date: 2023-10-12
     */
    Map<String, LocalDate> mapByOrgId(String category);

    /**
     * 查询单据日期是否已关账(单据已关账返回关账时间)
     *
     * @author Jim
     * @date: 2024-01-05
     */
    LocalDate checkClosed(String inventoryOrgId, LocalDate billDate);
    /**
     * @description: 根据组织id集合验证存货核算关账
     * @author Will
     * @date: 2024/2/28 20:10
     * @param orgIdList
     */
    void checkHsClosed (List<InventoryClosedRecordDTO.ClosedParamDTO> orgIdList);

    /**
     * 通过分类查询关账数据
     * @param category 分类
     * @return Map<String, InventoryClosedRecordEntity>
     */
    Map<String, InventoryClosedRecordEntity> mapByCategory(String category);
}
