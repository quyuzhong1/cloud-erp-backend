package com.erp.server.plm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.entity.BasicProductBuEntity;
import com.erp.server.plm.service.BasicProductBuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.model.plm.entity.ProductRefBuEntity;
import com.erp.server.plm.mapper.ProductRefBuMapper;
import com.erp.server.plm.service.ProductRefBuService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.plm.service.OperateLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;

import cn.hutool.core.collection.CollUtil;

/**
 * <p>
 * 产品bu信息关联表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2026-01-16
 */
@Slf4j
@Service
public class ProductRefBuServiceImpl extends SuperServiceImpl<ProductRefBuMapper, ProductRefBuEntity> implements ProductRefBuService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private BasicProductBuService basicProductBuService;

    @Override
    public List<ProductRefBuEntity> listByBuId(String id) {
        if(StrUtil.isEmpty(id)) {
            return Collections.emptyList();
        }
        return lambdaQuery().eq(ProductRefBuEntity::getBuId,id).list();
    }

    @Override
    public void addOrUpdate(String productId, String buId) {
        //查询是否存在
        ProductRefBuEntity existing = lambdaQuery()
                .eq(ProductRefBuEntity::getProductId, productId)
                .one();
        if (existing != null) {
            if(!existing.getBuId().equals(buId)) {
                existing.setBuId(buId);
                updateById(existing);
            }
        }else{
            ProductRefBuEntity newEntity = new ProductRefBuEntity();
            newEntity.setProductId(productId);
            newEntity.setBuId(buId);
            save(newEntity);
        }
    }

    @Override
    public List<ProductRefBuEntity> listByProductIds(List<String> productIdList) {
        if(CollUtil.isNotEmpty(productIdList)) {
            List<ProductRefBuEntity> productRefBuEntities = lambdaQuery().in(ProductRefBuEntity::getProductId, productIdList).list();
            if(CollUtil.isEmpty(productRefBuEntities)) {
                return Collections.emptyList();
            }
            List<String> buIds = productRefBuEntities.stream().map(ProductRefBuEntity::getBuId).distinct().collect(Collectors.toList());
            List<BasicProductBuEntity> basicProductBuEntities = basicProductBuService.listByIds(buIds);
            Map<String, String> buIdNameMap = basicProductBuEntities.stream()
                    .collect(Collectors.toMap(BasicProductBuEntity::getId, BasicProductBuEntity::getName));
            for (ProductRefBuEntity productRefBuEntity : productRefBuEntities) {
                productRefBuEntity.setBuName(buIdNameMap.get(productRefBuEntity.getBuId()));
            }
            return productRefBuEntities;
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProductRefBuEntity> listByBuNames(List<String> buNames) {
        if(CollUtil.isNotEmpty(buNames)) {
            List<BasicProductBuEntity> basicProductBuEntities = basicProductBuService.listByNames(buNames);
            if(CollUtil.isEmpty(basicProductBuEntities)) {
                return Collections.emptyList();
            }
            List<String> buIds = basicProductBuEntities.stream().map(BasicProductBuEntity::getId).distinct().collect(Collectors.toList());

            List<ProductRefBuEntity> productRefBuEntities = lambdaQuery().in(ProductRefBuEntity::getBuId, buIds).list();
            if(CollUtil.isEmpty(productRefBuEntities)) {
                return Collections.emptyList();
            }
            return productRefBuEntities;
        }

        return Collections.emptyList();
    }

    @Override
    public ProductRefBuEntity getByProductIds(String productId) {
        if(StrUtil.isEmpty(productId)) {
            return null;
        }
        List<ProductRefBuEntity> productRefBuEntities = this.listByProductIds(Collections.singletonList(productId));
        if(CollectionUtils.isEmpty(productRefBuEntities)){
            return null;
        }
        return productRefBuEntities.get(0);
    }

    @Override
    public void removeByProductId(String productId) {
        if (StrUtil.isEmpty(productId)) {
            return;
        }
        remove(new LambdaQueryWrapper<ProductRefBuEntity>().eq(ProductRefBuEntity::getProductId, productId));
    }

    @Override
    public void removeByProductIds(List<String> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return;
        }
        remove(new LambdaQueryWrapper<ProductRefBuEntity>().in(ProductRefBuEntity::getProductId, productIds));
    }

}
