package com.erp.server.oms.kingdee;

import com.erp.model.oms.entity.SoReturnEntity;

public interface SyncKingdeeSoReturnService {
    /**
     * 推送金蝶
     */
    String syncDataToKingdee(SoReturnEntity entity, String operate);
}
