package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductDetailApproverEntity;
import com.erp.server.plm.mapper.ProductDetailApproverMapper;
import com.erp.server.plm.service.ProductDetailApproverService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/28 17:10
 */
@Service
public class ProductDetailApproverServiceImpl extends ServiceImpl<ProductDetailApproverMapper, ProductDetailApproverEntity> implements ProductDetailApproverService {


    @Override
    public ProductDetailApproverEntity getProductDetailApprover() {
        LambdaQueryWrapper<ProductDetailApproverEntity> queryWrapper = new LambdaQueryWrapper<>();
        return this.getOne(queryWrapper);
    }
}
