package com.erp.server.dmp.service;

import com.erp.model.dmp.dto.DmpTransferInfoDTO;

/**
 * @author Will
 * @version 1.0
 * @description: 直接调拨单
 * @date 2023/7/4 18:55
 */
public interface DmpTransferInfoService {
    /**
     * @description:
     * @author Will
     * @date: 2023/7/4 18:57
     * @param ext
     */
    void sendSyncTask(DmpTransferInfoDTO ext);
}
