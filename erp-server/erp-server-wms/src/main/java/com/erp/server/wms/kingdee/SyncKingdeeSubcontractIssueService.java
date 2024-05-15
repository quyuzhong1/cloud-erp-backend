package com.erp.server.wms.kingdee;

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
    String syncDataToKingdee(SubcontractIssueEntity entity, String operate);
}
