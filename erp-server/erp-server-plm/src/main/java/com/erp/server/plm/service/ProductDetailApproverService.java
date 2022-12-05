package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProductDetailApproverEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/28 17:10
 */
public interface ProductDetailApproverService extends IService<ProductDetailApproverEntity> {

    /**
     * @description: 查询产品信息审核人配置
     * @author Will
     * @date: 2022/11/28 17:30
     * @return ProductDetailApproverEntity
     */
    ProductDetailApproverEntity getProductDetailApprover();

}
