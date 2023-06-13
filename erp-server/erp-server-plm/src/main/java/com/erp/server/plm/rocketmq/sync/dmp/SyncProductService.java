package com.erp.server.plm.rocketmq.sync.dmp;

import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;

import java.util.List;

/**
 * 同步plm产品信息
 * @Author Luo_WG
 * @Date 2023/4/19 14:02
 **/
public interface SyncProductService {

    /**
     * 同步产品信息表数据到中台表
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    void syncProductInfoToDmp(List<ProductInfoEntity> list);

    /**
     * 产品sku表同步到中台
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    void syncProductSkuToDmp(List<ProductDetailEntity> list);

    /**
     * 产品是否新品同步到中台
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    void syncNewProductToDmp();
}
