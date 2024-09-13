package com.erp.server.wms.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.entity.SubcontractIssueEntity;

/**
 * @description: 委外发料单
 * @author Will
 * @date: 2024/1/26 11:18
 */
public interface SyncKingdeeSubcontractIssueService {

    /**
     * 直接调拨单推送金蝶
     */
    DmpPushTaskEntity syncDataToKingdee(SubcontractIssueEntity entity, String operate);
    
    Map<String , Object> newSyncDataToKingdee(SubcontractIssueEntity entity, String operate);
}
