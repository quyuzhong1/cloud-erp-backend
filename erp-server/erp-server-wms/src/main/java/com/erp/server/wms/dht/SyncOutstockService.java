package com.erp.server.wms.dht;

import com.erp.model.oms.entity.CustomerInfoEntity;

public interface SyncOutstockService {
    /**
     * 创建推送客户任务
     */
    void createSyncSoOutstockTaskToDht(CustomerInfoEntity entity, String operate);
}
