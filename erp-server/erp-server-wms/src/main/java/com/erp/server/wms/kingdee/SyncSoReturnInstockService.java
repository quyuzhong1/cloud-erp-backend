package com.erp.server.wms.kingdee;

import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;

import java.util.Map;

public interface SyncSoReturnInstockService {
    /**
     * 同步数帝云字段映射处理
     */
    Map<String, Object> syncDataToSdyFieldHandler(SoReturnInstockEntity entity, SoReturnInstockDetailEntity soReturnInstockDetailEntity, String operate);

    /**
     * 同步数帝云
     */
    void syncDataToSdy(SoReturnInstockEntity entity, SoReturnInstockDetailEntity soReturnInstockDetailEntity, String operate);
}
