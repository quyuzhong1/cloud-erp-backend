package com.erp.server.oms.kingdee;

import com.erp.model.oms.entity.CustomerB2cContactEntity;

public interface SyncKingdeeCustomerB2cContactService {
    /**
     * 推送金蝶
     */
    void syncDataToKingdee(CustomerB2cContactEntity entity, String operate);
}
