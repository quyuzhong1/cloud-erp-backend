package com.erp.server.sys.rocketmq.sync.kingdee;

import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.erp.model.sys.entity.KingdeeOperatorRefPostEntity;
import com.erp.model.sys.entity.KingdeeUserRefPostEntity;

/**
 * @author Lambda
 * @Classname SyncKingdeeOperatorService
 * @Description TODO
 * @Date 2024-03-15 14:37
 * @Created by yl
 */
public interface SyncKingdeeOperatorService {

    /**
     * 同步业务员到金蝶
     * @description
     * @return
     * @date 2024-03-13 15:49
     * @author Lambda
     */
    void syncDataToKingdee(KingdeeOperatorRefPostEntity entity, String code);
}
