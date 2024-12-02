package com.erp.server.tms.sync;

import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;

import java.util.Map;

public interface SyncLogisticsBillService {
    /**
     * 同步数帝云字段映射处理
     */
    Map<String, Object> syncDataToSdyFieldHandler(LogisticsBillEntity entity, LogisticsBillDetailEntity detailEntity, String operate);

    /**
     * 同步数帝云
     */
    void syncDataToSdy(LogisticsBillEntity entity, LogisticsBillDetailEntity detailEntity, String operate);
}
