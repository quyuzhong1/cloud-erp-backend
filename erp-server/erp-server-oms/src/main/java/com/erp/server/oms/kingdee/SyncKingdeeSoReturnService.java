package com.erp.server.oms.kingdee;

import com.erp.model.oms.entity.SoChangeEntity;
import com.erp.model.oms.entity.SoReturnEntity;

public interface SyncKingdeeSoReturnService {
    /**
     * 推送金蝶
     */
    void syncDataToKingdee(SoReturnEntity entity, String operate);
}
