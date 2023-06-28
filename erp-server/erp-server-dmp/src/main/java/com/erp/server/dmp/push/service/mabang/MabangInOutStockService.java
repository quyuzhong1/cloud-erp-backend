package com.erp.server.dmp.push.service.mabang;

import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpOutInStockEntity;
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
     * 记录出入库
     * @param platformEntity
     * @param mabangInOutStock
     * @param transferInfo
     * @param sourceType
     * @param approveType
     */
    void inStock(PlatformEntity platformEntity,MabangInOutStockDTO mabangInOutStock, TransferInfoEntity transferInfo,
                 String sourceType, String approveType);

    /**
     * 记录出入库
     * @param platformEntity
     * @param mabangInOutStock
     * @param transferInfo
     * @param sourceType
     * @param approveType
     */
    void outStock(PlatformEntity platformEntity, MabangInOutStockDTO mabangInOutStock, TransferInfoEntity transferInfo,
                  String sourceType, String approveType);

    /**
     * 手工入库
     * @param dmpOutInStockEntity
     * @param mabangInOutStock
     * @param platformEntity
     */
    void sendToMabangInStock(DmpOutInStockEntity dmpOutInStockEntity, MabangInOutStockDTO mabangInOutStock, PlatformEntity platformEntity,
                             Integer type, String approveType);

    /**
     * 手工出库
     * @param dmpOutInStockEntity
     * @param mabangInOutStock
     * @param platformEntity
     */
    void sendToMabangOutStock(DmpOutInStockEntity dmpOutInStockEntity, MabangInOutStockDTO mabangInOutStock, PlatformEntity platformEntity,
                              Integer type, String approveType);

}
