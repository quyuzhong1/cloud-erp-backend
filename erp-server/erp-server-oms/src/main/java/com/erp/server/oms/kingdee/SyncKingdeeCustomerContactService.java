package com.erp.server.oms.kingdee;

import com.erp.model.oms.entity.CustomerContactEntity;

public interface SyncKingdeeCustomerContactService {
    /**
     * 推送金蝶
     */
    String syncDataToKingdee(CustomerContactEntity entity, String operate);
}
