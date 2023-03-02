package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductStatusTimeEntity;
import com.erp.server.plm.mapper.ProductStatusTimeMapper;
import com.erp.server.plm.service.ProductStatusTimeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:56
 */
@Service
public class ProductStatusTimeServiceImpl extends ServiceImpl<ProductStatusTimeMapper, ProductStatusTimeEntity>
        implements ProductStatusTimeService {

    @Override
    public void saveOrUpdateProductStatusTime(String productId, Integer approvalStatus) {
        ProductStatusTimeEntity productStatusTimeEntity = getByProductIdAndStatus(productId, approvalStatus);
        if (ObjectUtils.isNotEmpty(productStatusTimeEntity)) {
            //存在则更新状态时间
            productStatusTimeEntity.setStatusTime(LocalDateTime.now());
        } else {
            //不存在则新增
            productStatusTimeEntity = new ProductStatusTimeEntity();
            productStatusTimeEntity.setProductId(productId);
            productStatusTimeEntity.setStatusTime(LocalDateTime.now());
            productStatusTimeEntity.setStatus(String.valueOf(approvalStatus));
        }
         this.saveOrUpdate(productStatusTimeEntity);
    }

    @Override
    public List<ProductStatusTimeEntity> listByProductId(String productId) {
        LambdaQueryWrapper<ProductStatusTimeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductStatusTimeEntity::getProductId,productId);
        return this.list(queryWrapper);
    }

    /**
     * @description: 根据产品id和状态查询
     * @author Will
     * @date: 2023/2/24 10:57
     * @param productId
     * @param approvalStatus
     * @return ProductStatusTimeEntity
     */
    private ProductStatusTimeEntity getByProductIdAndStatus(String productId, Integer approvalStatus) {
        LambdaQueryWrapper<ProductStatusTimeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductStatusTimeEntity::getProductId,productId);
        queryWrapper.eq(ProductStatusTimeEntity::getStatus,String.valueOf(approvalStatus));
        return this.getOne(queryWrapper);
    }
}
