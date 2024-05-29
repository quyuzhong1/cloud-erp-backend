package com.erp.server.oms.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.CustomerContactEntity;

public interface SyncKingdeeCustomerContactService {
    /**
     * 推送金蝶
     */
    DmpPushTaskEntity syncDataToKingdee(CustomerContactEntity entity, String operate);
}
