package com.erp.server.dmp.push.service.mabang;

import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
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
     * 手工出入库
     * @param dmpPushTaskEntity
     * @param mabangInOutStock
     * @param inventoryInOutEnum
     */
    void sendToMabangInOutStock(DmpPushTaskEntity dmpPushTaskEntity, MabangInOutStockDTO mabangInOutStock, InventoryInOutEnum inventoryInOutEnum);

    /**
     * 未配置监控仓库通知
     * @param sourceTypeName
     */
    void sendNoticeNoMonitorWarehouse(String sourceTypeName);

    /**
     * 未找到同步任务通知
     * @param syncTaskId
     * @param erpSourceCode
     */
    void sendNoTaskNotice(String syncTaskId, String erpSourceCode);

}
