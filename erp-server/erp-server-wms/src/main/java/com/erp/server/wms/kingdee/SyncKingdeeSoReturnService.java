package com.erp.server.wms.kingdee;

import com.erp.model.wms.entity.SoReturnInstockEntity;

public interface SyncKingdeeSoReturnService {
    /**
     * 推送金蝶
     */
    void syncDataToKingdee(SoReturnInstockEntity entity, String operate);
}
