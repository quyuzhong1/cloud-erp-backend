package com.erp.server.wms.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;

public interface SyncKingdeeSoReturnService {
    /**
     * 推送金蝶
     */
    DmpPushTaskEntity syncDataToKingdee(SoReturnInstockEntity entity, String operate);
}
