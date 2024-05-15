package com.erp.server.oms.kingdee;

import com.erp.model.oms.entity.CustomerGroupEntity;

public interface SyncKingdeeCustomerGroupService {
    /**
     * 推送金蝶
     */
    String syncDataToKingdee(CustomerGroupEntity entity, String operate);
}
