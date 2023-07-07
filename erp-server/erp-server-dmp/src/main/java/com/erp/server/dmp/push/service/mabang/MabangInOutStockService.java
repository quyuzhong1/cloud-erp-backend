package com.erp.server.dmp.push.service.mabang;

import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpSyncTaskEntity;
import com.erp.model.wms.enums.inventory.InventoryInOutEnum;

import java.util.List;


/**
 * @Classname: MabangInOutStockService
 * @Description: 马帮手工出入库接口
 * @CreateTime: 2023-06-27  19:50
 * @Author: zhangchunlin
 */
public interface MabangInOutStockService {

    /**
     * 记录出入库
     * @param mabangInOutStock
     * @param sourceId
     * @param sourceCode
     * @param sourceType
     * @param approveType
     */
    void inOutStock(MabangInOutStockDTO mabangInOutStock, String sourceId, String sourceCode,
                 String sourceType, String approveType);

    /**
     * 记录出入库（批量）
     * @param mabangInOutStockDTOList
     * @param sourceId
     * @param sourceCode
     * @param sourceType
     * @param approveType
     */
    void batchInOutStock(List<MabangInOutStockDTO> mabangInOutStockDTOList, String sourceId, String sourceCode,
                         String sourceType, String approveType);


    /**
     * 手工入库
     * @param dmpSyncTaskEntity
     * @param mabangInOutStock
     */
    void sendToMabangInStock(DmpSyncTaskEntity dmpSyncTaskEntity, MabangInOutStockDTO mabangInOutStock);

    /**
     * 手工出库
     * @param dmpSyncTaskEntity
     * @param mabangInOutStock
     */
    void sendToMabangOutStock(DmpSyncTaskEntity dmpSyncTaskEntity, MabangInOutStockDTO mabangInOutStock);


    /**
     * 手工出入库
     * @param dmpSyncTaskEntity
     * @param mabangInOutStock
     * @param inventoryInOutEnum
     */
    void sendToMabangInOutStock(DmpSyncTaskEntity dmpSyncTaskEntity, MabangInOutStockDTO mabangInOutStock, InventoryInOutEnum inventoryInOutEnum);

}
