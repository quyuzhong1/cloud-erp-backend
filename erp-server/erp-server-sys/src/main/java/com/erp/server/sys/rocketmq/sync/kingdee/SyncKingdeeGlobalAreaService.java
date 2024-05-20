package com.erp.server.sys.rocketmq.sync.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;

/**
 * @author Lambda
 * @Classname SyncKingdeeGlobalAreaService
 * @Description TODO
 * @Date 2024-03-19 14:39
 * @Created by yl
 */
public interface SyncKingdeeGlobalAreaService {

    /**
     * 同步金蝶
     * @param entity
     * @param
     */
    DmpPushTaskEntity syncDataToKingdee(DictGlobalAreaEntity entity, String operate);
}
