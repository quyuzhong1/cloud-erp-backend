package com.erp.server.wms.dht;

import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;

import java.util.List;

public interface SyncDhtOutstockService {
    /**
     * 创建推送客户任务
     */
    void syncB2bSoOutstockDht(SoOutstockEntity entity, List<SoOutstockDetailEntity> soOutstockDetailEntityList, String operate);
}
