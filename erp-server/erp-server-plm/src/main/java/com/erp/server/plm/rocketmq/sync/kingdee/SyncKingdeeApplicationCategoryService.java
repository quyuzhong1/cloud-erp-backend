package com.erp.server.plm.rocketmq.sync.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.entity.ApplicationCategoryEntity;

import java.util.Map;

public interface SyncKingdeeApplicationCategoryService {
    /**
     * 金蝶同步数据
     * @author Will
     * @date: 2023/3/8 18:22
     * @param entity
     */
    DmpPushTaskEntity syncDataToKingdee(ApplicationCategoryEntity entity, String operate);

    Map<String, Object> newSyncDataToKingdee(ApplicationCategoryEntity entity, String operate);
}
