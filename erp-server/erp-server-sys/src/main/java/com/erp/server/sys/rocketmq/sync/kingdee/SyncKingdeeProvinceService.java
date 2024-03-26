package com.erp.server.sys.rocketmq.sync.kingdee;

import com.erp.model.sys.entity.DictCityEntity;

/**
 * @author Lambda
 * @Classname SyncKingdeeProvinceService
 * @Description TODO
 * @Date 2024-03-20 11:10
 * @Created by yl
 */
public interface SyncKingdeeProvinceService {

    /**
     * 同步金蝶
     * @param
     * @param
     */
    void syncDataToKingdee(DictCityEntity addEntity, String operate);
}
