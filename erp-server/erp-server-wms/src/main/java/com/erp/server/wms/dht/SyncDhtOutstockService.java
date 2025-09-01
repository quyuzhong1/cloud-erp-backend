package com.erp.server.wms.dht;

import com.erp.model.wms.entity.SoOutstockEntity;

public interface SyncDhtOutstockService {
    /**
     * 创建推送客户任务
     */
    void syncB2bSoOutstockDht(SoOutstockEntity entity, String operate);
}
