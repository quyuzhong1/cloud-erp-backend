package com.erp.server.wms.kingdee;

import com.erp.model.wms.entity.SoReturnInstockEntity;

public interface SyncKingdeeSoReturnService {
    /**
     * 推送金蝶
     */
    String syncDataToKingdee(SoReturnInstockEntity entity, String operate);
}
