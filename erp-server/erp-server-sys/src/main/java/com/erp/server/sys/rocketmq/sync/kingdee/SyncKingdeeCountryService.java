package com.erp.server.sys.rocketmq.sync.kingdee;

import com.erp.model.sys.entity.DictCountryEntity;

/**
 * @author Lambda
 * @Classname SyncKingdeeCountryService
 * @Description
 * @Date 2024-03-19 17:35
 * @Created by yl
 */
public interface SyncKingdeeCountryService {

    /**
     * 同步金蝶
     * @param entity
     * @param
     */
    void syncDataToKingdee(DictCountryEntity entity, String code);
}
