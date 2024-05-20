package com.erp.server.oms.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.CustomerGroupEntity;

public interface SyncKingdeeCustomerGroupService {
    /**
     * 推送金蝶
     */
    DmpPushTaskEntity syncDataToKingdee(CustomerGroupEntity entity, String operate);
}
