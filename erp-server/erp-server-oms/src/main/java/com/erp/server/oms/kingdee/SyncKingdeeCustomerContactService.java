package com.erp.server.oms.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.CustomerContactEntity;

public interface SyncKingdeeCustomerContactService {
    /**
     * 推送金蝶
     */
    DmpPushTaskEntity syncDataToKingdee(CustomerContactEntity entity, String operate);
    
    Map<String , Object> newSyncDataToKingdee(CustomerContactEntity entity, String operate);
}
