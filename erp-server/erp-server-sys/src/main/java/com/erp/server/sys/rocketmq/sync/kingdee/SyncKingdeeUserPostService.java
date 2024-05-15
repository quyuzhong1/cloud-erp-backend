package com.erp.server.sys.rocketmq.sync.kingdee;

import com.erp.model.sys.entity.KingdeeUserRefPostEntity;

/**
 * @author Lambda
 * @Classname SyncKingdeeUserPostService
 * @Description TODO
 * @Date 2024-03-14 14:15
 * @Created by yl
 */
public interface SyncKingdeeUserPostService {

    /**
     * 同步岗位数据到金蝶
     * @description
     * @return
     * @date 2024-03-13 15:49
     * @author Lambda
     */
    String syncDataToKingdee(KingdeeUserRefPostEntity entity, String code);

}
