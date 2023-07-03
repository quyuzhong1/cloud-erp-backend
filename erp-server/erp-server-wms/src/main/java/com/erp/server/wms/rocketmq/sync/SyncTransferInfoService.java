package com.erp.server.wms.rocketmq.sync;

import com.erp.model.dmp.dto.DmpTransferInfoDTO;

/**
 * @author Will
 * @version 1.0
 * @description: 直接调拨单同步service
 * @date 2023/6/29 11:07
 */
public interface SyncTransferInfoService {
    /**
     * @description: 同步直接调拨单
     * @author Will
     * @date: 2023/6/29 11:08
     * @param entity
     */
    void syncKingdeeTransferInfo(DmpTransferInfoDTO entity);
}
