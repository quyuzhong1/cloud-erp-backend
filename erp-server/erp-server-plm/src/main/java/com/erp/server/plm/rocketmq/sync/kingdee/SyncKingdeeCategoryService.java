package com.erp.server.plm.rocketmq.sync.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.entity.BasicCategoryEntity;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/4 12:26
 */
public interface SyncKingdeeCategoryService {

    /**
     * 金蝶同步数据
     * @author Will
     * @date: 2023/3/8 18:22
     * @param entity
     */
    DmpPushTaskEntity syncDataToKingdee(BasicCategoryEntity entity, String operate);
    
    Map<String, Object> newSyncDataToKingdee(BasicCategoryEntity entity, String operate);
}
