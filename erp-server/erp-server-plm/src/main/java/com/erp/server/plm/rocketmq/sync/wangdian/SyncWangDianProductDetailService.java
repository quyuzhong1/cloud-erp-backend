package com.erp.server.plm.rocketmq.sync.wangdian;


import com.erp.model.plm.entity.ProductDetailEntity;

import java.util.List;

public interface SyncWangDianProductDetailService {

    void syncDataToWangDian(ProductDetailEntity entity);

    void syncDataToWangDian(List<ProductDetailEntity> entityList);

    void addPlmPushMsg(ProductDetailEntity entity);
}
