package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.InventoryDTO;
import com.erp.model.wms.entity.InventoryEntity;

/**
 * @Classname: InventoryService
 * @Description: TODO
 * @CreateTime: 2023-04-25  12:15
 * @Author: zhangchunlin
 */
public interface InventoryService extends SuperService<InventoryEntity> {

    /**
     * 库存交易业务处理
     * @param inventoryDTO
     */
    void trade(InventoryDTO.AddDTO inventoryDTO);

}
