package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductUnitEntity;
import com.erp.model.plm.entity.ProductVariantOptionEntity;
import com.erp.server.plm.mapper.ProductVariantOptionMapper;
import com.erp.server.plm.service.ProductVariantOptionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class ProductVariantOptionServiceImpl extends ServiceImpl<ProductVariantOptionMapper, ProductVariantOptionEntity>
    implements ProductVariantOptionService {


    /**
     * @Description 产品选择的变体查询
     * @Author Luo_WG
     * @Date 2022/9/26 14:06
     * @param productId:产品表id
     * @return java.util.List<com.erp.model.plm.dto.ProductVariantPropertyEntity>
     **/
    @Override
    public List<ProductVariantOptionEntity> list(String productId) {
        LambdaQueryWrapper<ProductVariantOptionEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductVariantOptionEntity::getProductId, productId);
        return this.list(queryWrapper);
    }

    /**
     * @Description 保存/修改产品选择的变体类型值信息
     * @Author Luo_WG
     * @Date 2022/9/26 10:13
     * @param dto 请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateOptionEntity(ProductVariantOptionEntity dto) {
        return this.saveOrUpdate(dto);
    }

    /**
     * @Description 保存/修改产品选择的变体类型值信息-批量
     * @Author Luo_WG
     * @Date 2022/9/26 10:13
     * @param dto 请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateOptionEntityBatch(List<ProductVariantOptionEntity> dto) {
        return this.saveOrUpdateBatch(dto);
    }

    /**
     * @Description 保存/修改产品选择的变体类型值信息-批量
     * @Author Luo_WG
     * @Date 2022/9/26 10:13
     * @param productId:产品表id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean deleteByProductId(String productId) {
        LambdaQueryWrapper<ProductVariantOptionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductVariantOptionEntity::getProductId, productId);
        return this.remove(queryWrapper);
    }

    @Override
    public List<ProductVariantOptionEntity> getByProductId(String productId) {
        LambdaQueryWrapper<ProductVariantOptionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductVariantOptionEntity::getProductId, productId);
        return this.list(queryWrapper);
    }
}




