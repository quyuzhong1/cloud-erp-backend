package com.erp.server.dmp.push.service.mabang;

import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.wms.entity.TransferInfoEntity;



/**
 * @Classname: MabangInOutStockService
 * @Description: 马帮手工出入库接口
 * @CreateTime: 2023-06-27  19:50
 * @Author: zhangchunlin
 */
public interface MabangInOutStockService {

    /**
     * 手工入库
     * @param platformEntity
     * @param mabangInOutStock
     * @param transferInfo
     * @param sourceType
     */
    void inStock(PlatformEntity platformEntity,MabangInOutStockDTO mabangInOutStock, TransferInfoEntity transferInfo, String sourceType);

    /**
     * 手工出库
     * @param platformEntity
     * @param mabangInOutStock
     * @param transferInfo
     * @param sourceType
     */
    void outStock(PlatformEntity platformEntity, MabangInOutStockDTO mabangInOutStock, TransferInfoEntity transferInfo, String sourceType);

}
