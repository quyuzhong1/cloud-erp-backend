package com.erp.server.oms.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;

import java.util.List;
import java.util.Map;

public interface SyncKingdeeCustomerService {
    /**
     * 推送金蝶
     */
    List<DmpPushTaskEntity> syncDataToKingdee(CustomerInfoEntity entity, String operate);
    
    void syncDataToSdy(CustomerInfoEntity entity, String operate);
    
    Map<String, Object> newSyncDataToKingdee(CustomerInfoEntity entity, String operate);
    
    Map<String, Object> newSyncDataToSdy(CustomerInfoEntity entity, String operate);
}
