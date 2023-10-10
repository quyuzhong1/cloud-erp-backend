package com.erp.server.oms.kingdee;

import com.erp.model.oms.entity.CustomerB2cGroupEntity;

public interface SyncKingdeeCustomerB2cGroupService {
    /**
     * 推送金蝶
     */
    void syncDataToKingdee(CustomerB2cGroupEntity entity, String operate);
}
