package com.erp.server.plm.rocketmq.sync.kingdee;

import com.erp.model.plm.entity.BomInfoEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/9 9:41
 */
public interface SyncKingdeeBomInfoService {
    /**
     * 金蝶同步数据
     * @author Will
     * @date: 2023/3/8 18:22
     * @param entity
     */
    void syncDataToKingdee(BomInfoEntity entity);
}
