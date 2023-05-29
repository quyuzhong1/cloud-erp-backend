package com.erp.server.oms.kingdee;

import com.erp.model.oms.entity.CustomerInfoEntity;

public interface SyncKingdeeCustomerGroupService {
    /**
     * 推送金蝶
     */
    void syncDataToKingdee(CustomerInfoEntity entity, String operate);
}
