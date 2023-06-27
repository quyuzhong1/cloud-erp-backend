package com.erp.server.plm.service.impl;

import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.mapper.ProductCustomsMapper;
import com.erp.server.plm.service.ProductCustomsService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.plm.service.ProductDetailService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-12
 */
@Slf4j
@Service
public class ProductCustomsServiceImpl extends SuperServiceImpl<ProductCustomsMapper, ProductCustomsEntity> implements ProductCustomsService {

    @Resource
    private ProductDetailService productDetailService;

    @Override
    public List<ProductCustomsEntity> listByProductId(String productId) {
        return baseMapper.listByProductId(productId);
    }

    @Override
    public Boolean removeBySkuId(List<String> skuIds) {
        return lambdaUpdate().set(ProductCustomsEntity::getIsDeleted, Boolean.TRUE).in(ProductCustomsEntity::getSkuId, skuIds).update();
    }

    @Override
    public Boolean addProductCustoms() {
        List<ProductDetailEntity> list = productDetailService.list();
        List<ProductCustomsEntity> customsEntityList = new ArrayList<>();
        list.forEach(req -> {
            ProductCustomsEntity productCustomsEntity = new ProductCustomsEntity();
            productCustomsEntity.setSkuId(req.getId());
            customsEntityList.add(productCustomsEntity);
        });
        return this.saveBatch(customsEntityList);
    }
}
