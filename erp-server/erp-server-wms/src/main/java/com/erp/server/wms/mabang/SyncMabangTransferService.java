package com.erp.server.wms.mabang;

import com.erp.model.wms.entity.TransferInfoEntity;

/**
 * @Classname: SyncMabangTransferService
 * @Description: 直接调拨单推送到马帮
 * @CreateTime: 2023-06-27  16:33
 * @Author: zhangchunlin
 */
public interface SyncMabangTransferService {

    /**
     * 直接调拨单推送到马帮
     */
    void syncDataToMabang(TransferInfoEntity entity, String operate);

}
