package com.erp.server.wms.kingdee;

import com.erp.model.wms.entity.SoOutstockEntity;

/**
 * 销售出库单
 * @Author Luo_WG
 * @Date 2023/5/31 16:41
 **/
public interface SyncKingdeeSoOutstockService {
    /**
     * 发送消息同步金蝶
     * @Author Luo_WG
     * @Date 2023/4/24 11:27
     * @param entity
     * @param operate
     * @return void
     **/
    String syncDataToKingdee(SoOutstockEntity entity, String operate);

    /**
     * 发送消息同步金蝶
     * @description
     * @param entity
     * @param
     * @author Lambda
     * @return
     * @create 2023-12-27 16:50
     */
    String syncB2cDataToKingdee(SoOutstockEntity entity, String operate);
    /**
     * 推送订单到mq
     *
     * @param entity
     * @param syncOperate
     */
    void syncOrderToDmp(SoOutstockEntity entity, String syncOperate);
}
