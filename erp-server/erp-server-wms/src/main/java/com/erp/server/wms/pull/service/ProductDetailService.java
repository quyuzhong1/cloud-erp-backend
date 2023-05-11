package com.erp.server.wms.pull.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProductDetailEntity;

public interface ProductDetailService extends IService<ProductDetailEntity> {

    /**
     * 更新PLM同步过来的数据
     * @Author zhangchunlin
     * @Date 2023-05-11 18:07
     **/
    Boolean saveOrUpdateProductDetail(ProductDetailEntity productDetailEntity);
}
