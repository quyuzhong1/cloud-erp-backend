package com.erp.server.plm.rocketmq.sync.wangdian;


import com.erp.model.plm.entity.ProductDetailEntity;

import java.util.List;

public interface SyncWangDianProductDetailService {

    void syncDataToWangDian(ProductDetailEntity entity);

    void syncDataToWangDian(List<ProductDetailEntity> entityList);

    void addPlmPushMsg(ProductDetailEntity entity);

    /**
     * 同步失败时写入本地补偿消息，便于后续人工/定时任务重试。
     */
    void saveSyncErrorPushMsg(ProductDetailEntity entity, String remark);
}
