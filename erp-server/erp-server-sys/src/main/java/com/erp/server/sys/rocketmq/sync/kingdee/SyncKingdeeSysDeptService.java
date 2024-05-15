package com.erp.server.sys.rocketmq.sync.kingdee;

import com.erp.model.sys.entity.KingdeeDepartmentEntity;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/10 14:40
 */
public interface SyncKingdeeSysDeptService {

    /**
     * @description:新增同步
     * @author Will
     * @date: 2023/4/11 18:06
     * @param entity
     * @param operate
     */
    String syncDataToKingdee(KingdeeDepartmentEntity entity, String operate);
}
