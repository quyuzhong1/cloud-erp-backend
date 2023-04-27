package com.erp.server.plm.rocketmq.sync.kingdee;

import com.erp.model.plm.entity.BasicCategoryEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/4 12:26
 */
public interface SyncKingdeeCategoryService {

    /**
     * 金蝶同步数据
     * @author Will
     * @date: 2023/3/8 18:22
     * @param entity
     */
    void syncDataToKingdee(BasicCategoryEntity entity,String operate);
}
