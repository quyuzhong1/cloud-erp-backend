package com.erp.server.plm.rocketmq.sync.wangdian;


import com.erp.model.plm.entity.ProductDetailEntity;

public interface SyncWangDianProductDetailService {

    void syncDataToWangDian(ProductDetailEntity entity);
}
