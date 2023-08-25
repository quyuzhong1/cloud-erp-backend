package com.erp.server.oms.kingdee;

import com.erp.model.oms.entity.CustomerB2cEntity;

public interface SyncKingdeeCustomerB2cService {
    /**
     * 推送金蝶
     */
    void syncDataToKingdee(CustomerB2cEntity entity, String operate);
}
