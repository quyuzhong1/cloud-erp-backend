package com.erp.server.wms.rocketmq.sync;

import com.erp.model.dmp.entity.DmpFbaDeliveryEntity;

/**
 * @Classname: SyncFbaDeliveryService
 * @Description: 同步FBA发货单生成加工单
 * @CreateTime: 2023-06-30  14:34
 * @Author: zhangchunlin
 */
public interface SyncFbaDeliveryService {

    /**
     * @description: 同步FBA发货单生成加工单
     * @param entity
     */
    void syncFbaDelivery(DmpFbaDeliveryEntity entity, String sourceType);

}
