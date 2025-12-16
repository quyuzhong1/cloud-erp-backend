package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.ProductBrandDTO;
import com.erp.model.plm.entity.ProductBrandEntity;
import com.erp.server.plm.mapper.ProductBrandMapper;
import com.erp.server.plm.service.ProductBrandService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产品品牌服务实现类
 * @Author Auto
 * @Date 2025/01/20
 **/
@Service
public class ProductBrandServiceImpl extends ServiceImpl<ProductBrandMapper, ProductBrandEntity>
    implements ProductBrandService {

    /**
     * 保存/修改产品品牌
     * @param productBrandList 产品品牌新增信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductBrandDTO> productBrandList) {
        List<ProductBrandEntity> productBrandEntities = BeanMapper.copyList(productBrandList, ProductBrandEntity.class);
        // 校验 name + is_deleted 唯一性（只检查未删除的记录）
        for (ProductBrandEntity entity : productBrandEntities) {
            if (entity.getName() != null) {
                LambdaQueryWrapper<ProductBrandEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ProductBrandEntity::getName, entity.getName());
                queryWrapper.eq(ProductBrandEntity::getIsDeleted, Boolean.FALSE);
                // 如果是更新操作，排除自身
                if (entity.getId() != null) {
                    queryWrapper.ne(ProductBrandEntity::getId, entity.getId());
                }
                ProductBrandEntity existEntity = this.getOne(queryWrapper);
                if (existEntity != null) {
                    throw new ServiceException(ApiError.COMMON_DUPLICATION_NAME);
                }
            }
        }
        return this.saveOrUpdateBatch(productBrandEntities);
    }

    /**
     * 查询产品品牌
     * @return java.util.List<com.erp.model.plm.entity.ProductBrandEntity>
     **/
    @Override
    public List<ProductBrandEntity> listProductBrand(){
        LambdaQueryWrapper<ProductBrandEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductBrandEntity::getIsDeleted, Boolean.FALSE);
        return this.list(queryWrapper);
    }

    /**
     * 删除产品品牌
     * @param id 主键id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean delete(String id){
        ProductBrandEntity entity = this.getById(id);
        if (entity.getOccupyStatus()) {
            throw new ServiceException(ApiError.PRODUCT_VARIANT_VALUES_REF_DELETE_FORBIDDEN);
        }
        LambdaQueryWrapper<ProductBrandEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductBrandEntity::getId, id);
        return this.remove(queryWrapper);
    }

    /**
     * 查询品牌名称是否存在
     * @param name 品牌名称
     * @return ProductBrandEntity
     **/
    @Override
    public ProductBrandEntity checkBrandName(String name){
        LambdaQueryWrapper<ProductBrandEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductBrandEntity::getName, name);
        queryWrapper.eq(ProductBrandEntity::getIsDeleted, Boolean.FALSE);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 设置占用
     * @Author Auto
     * @Date 2025/01/20
     * @param ids
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean setupOccupy(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().set(ProductBrandEntity::getOccupyStatus, Boolean.TRUE).in(ProductBrandEntity::getId, ids).update();
    }

    @Override
    public ProductBrandEntity getByName(String name) {
        ProductBrandEntity entity = lambdaQuery().eq(ProductBrandEntity::getName, name)
                .eq(ProductBrandEntity::getIsDeleted, Boolean.FALSE)
                .last("limit 1")
                .one();
        return entity;
    }
}

