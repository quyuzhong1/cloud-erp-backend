package com.erp.server.sys.rocketmq.sync.kingdee;

import com.erp.model.sys.entity.DictCityEntity;

/**
 * @author Lambda
 * @Classname SyncKingdeeCityService
 * @Description TODO
 * @Date 2024-03-20 14:31
 * @Created by yl
 */
public interface SyncKingdeeCityService {

    /**
     * 同步金蝶
     * @param
     * @param
     */
    void syncDataToKingdee(DictCityEntity addEntity, String operate);
}
