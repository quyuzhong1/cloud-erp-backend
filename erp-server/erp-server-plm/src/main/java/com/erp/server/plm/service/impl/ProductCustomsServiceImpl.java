package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.dto.ProductCustomsSkuDTO;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.mapper.ProductCustomsMapper;
import com.erp.server.plm.service.ProductCustomsService;
import com.erp.server.plm.service.ProductDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    public List<ProductCustomsEntity> listBySkuId(String skuId) {
        return baseMapper.listBySkuId(skuId);
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

    /**
     * 获取sku定义的目的国申报海关编码
     * @param dto
     * @return
     */
    @Override
    public List<ProductCustomsEntity> listProductCustomsBySkuIds(ProductCustomsSkuDTO dto) {
        if (Objects.isNull(dto) || CollectionUtil.isEmpty(dto.getSkuIds())){
            return Collections.emptyList();
        }
        return baseMapper.listProductCustomsBySkuIds(dto);
    }
}
