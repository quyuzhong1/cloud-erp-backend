package com.erp.server.scm.kingdee;

import com.erp.model.scm.entity.SubcontractChangeEntity;

/**
 * @author Will
 * @version 1.0
 * @date 2023/6/20 16:37
 */
public interface SyncKingdeeSubcontractChangeService {

    String syncDataToKingdee(SubcontractChangeEntity entity, String syncOperate);
}
