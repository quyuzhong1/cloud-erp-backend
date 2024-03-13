package com.erp.server.sys.rocketmq.sync.kingdee;

import com.erp.model.sys.entity.KingdeePostEntity;

/**
 * @author Lambda
 * @Classname SyncKingdeePostService
 * @Description TODO
 * @Date 2024-03-13 15:48
 * @Created by yl
 */
public interface SyncKingdeePostService {
    
    /**
     * 同步岗位数据到金蝶
     * @description
     * @return
     * @date 2024-03-13 15:49
     * @author Lambda
     */
    void syncDataToKingdee(KingdeePostEntity kingdeePostEntity, String code);
}
