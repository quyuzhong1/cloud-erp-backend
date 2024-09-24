package com.erp.server.scm.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.scm.entity.SubcontractChangeEntity;

/**
 * @author Will
 * @version 1.0
 * @date 2023/6/20 16:37
 */
public interface SyncKingdeeSubcontractChangeService {

    DmpPushTaskEntity syncDataToKingdee(SubcontractChangeEntity entity, String syncOperate);
    
    Map<String , Object> newSyncDataToKingdee(SubcontractChangeEntity entity, String syncOperate);
}
