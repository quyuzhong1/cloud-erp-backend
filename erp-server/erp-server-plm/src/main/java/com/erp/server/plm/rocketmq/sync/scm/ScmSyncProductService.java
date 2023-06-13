package com.erp.server.plm.rocketmq.sync.scm;

import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;

import java.util.List;

public interface ScmSyncProductService {
    /**
     * 同步产品信息表数据到scm
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    void syncProductInfoToScm(List<ProductInfoEntity> list);

    /**
     * 同步产品Sku信息表数据到scm
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    void syncProductSkuToScm(List<ProductDetailEntity> list);
}
