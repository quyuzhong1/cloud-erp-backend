package com.erp.server.wms.service;

import com.erp.model.wms.entity.InventoryClosedRecordEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.inventory.InventoryClosedRecordDTO;

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
    Map<String, LocalDate> mapByOrgId();
}
