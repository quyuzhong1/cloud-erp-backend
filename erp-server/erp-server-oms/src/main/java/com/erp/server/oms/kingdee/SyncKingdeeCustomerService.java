package com.erp.server.oms.kingdee;

import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.wms.entity.OtherInstockEntity;

public interface SyncKingdeeCustomerService {
    /**
     * 推送金蝶
     */
    void syncDataToKingdee(SoReturnEntity entity, String operate);
}
