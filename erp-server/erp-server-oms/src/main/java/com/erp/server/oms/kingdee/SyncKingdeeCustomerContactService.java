package com.erp.server.oms.kingdee;

import com.erp.model.oms.dto.CustomerContactDTO;
import com.erp.model.oms.entity.CustomerContactEntity;

public interface SyncKingdeeCustomerContactService {
    /**
     * 推送金蝶
     */
    void syncDataToKingdee(CustomerContactEntity entity, String operate);
}
