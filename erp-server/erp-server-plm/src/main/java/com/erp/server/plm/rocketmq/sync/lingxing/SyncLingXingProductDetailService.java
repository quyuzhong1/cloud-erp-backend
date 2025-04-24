package com.erp.server.plm.rocketmq.sync.lingxing;


import com.erp.model.plm.entity.PlmPushMsgEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.sdk.third.lingxing.dto.ProductInfo;

import java.util.List;

public interface SyncLingXingProductDetailService {

    void syncDataToLingxing(ProductDetailEntity entity);

    void syncDataToLingxing(List<ProductDetailEntity> entityList);

    void addPlmPushMsg(ProductDetailEntity entity);

    PlmPushMsgEntity createPlmPushMsgEntity(ProductDetailEntity entity);

    ProductInfo convertProductInfo(ProductDetailEntity entity);
}
