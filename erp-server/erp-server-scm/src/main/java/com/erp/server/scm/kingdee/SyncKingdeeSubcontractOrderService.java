package com.erp.server.scm.kingdee;

import com.erp.model.scm.entity.SubcontractOrderEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/6/20 16:37
 */
public interface SyncKingdeeSubcontractOrderService {

    void syncDataToKingdee(SubcontractOrderEntity entity, String syncOperate);
}
