package com.erp.server.plm.rocketmq.sync.wms;

import com.erp.model.plm.entity.ProductDetailEntity;

import java.util.List;

/**
 * @Classname: WmsSyncProductService
 * @Description: TODO
 * @CreateTime: 2023-05-11  19:12
 * @Author: zhangchunlin
 */
public interface WmsSyncProductService {

    /**
     * 产品sku表同步到WMS仓储服务
     * @Author zhangchunlin
     * @Date 2023-05-11 18:41
     **/
    void syncProductSkuToWms(List<ProductDetailEntity> list);

    /**
     * 产品sku销售表同步到WMS仓储服务
     * @Author zhangchunlin
     * @Date 2023-05-11 18:41
     **/
    void syncProductSkuSaleToWms();

    /**
     * 同步产品信息表数据到WMS仓储服务
     * @Author zhangchunlin
     * @Date 2023-05-11 18:41
     **/
    void syncProductInfoToWms();

}
