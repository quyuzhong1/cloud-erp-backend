package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.ProductUnitDTO;
import com.erp.model.plm.entity.ProductUnitEntity;
import com.erp.server.plm.mapper.ProductUnitMapper;
import com.erp.server.plm.service.ProductUnitService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 产品信息服务类
 * @Author Luo_WG
 * @Date 2022/9/27 15:14
 **/
@Service
public class ProductUnitServiceImpl extends ServiceImpl<ProductUnitMapper, ProductUnitEntity>
    implements ProductUnitService {

    /**
     * @Description 保存/修改产品单位
     * @Author Luo_WG
     * @Date 2022/9/27 15:08
     * @param productUnitList:产品单位新增信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductUnitDTO> productUnitList) {
        List<ProductUnitEntity> productUnitEntities = BeanMapper.copyList(productUnitList, ProductUnitEntity.class);
        // 校验 name + is_deleted 唯一性（只检查未删除的记录）
        for (ProductUnitEntity entity : productUnitEntities) {
            if (entity.getName() != null) {
                LambdaQueryWrapper<ProductUnitEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ProductUnitEntity::getName, entity.getName());
                queryWrapper.eq(ProductUnitEntity::getIsDeleted, Boolean.FALSE);
                // 如果是更新操作，排除自身
                if (entity.getId() != null) {
                    queryWrapper.ne(ProductUnitEntity::getId, entity.getId());
                }
                ProductUnitEntity existEntity = this.getOne(queryWrapper);
                if (existEntity != null) {
                    throw new ServiceException(ApiError.ERROR_DUPLICATION_NAME);
                }
            }
        }
        return this.saveOrUpdateBatch(productUnitEntities);
    }

    /**
     * @Description 查询产品单位
     * @Author Luo_WG
     * @Date 2022/9/27 15:02
     * @return java.util.List<com.erp.model.plm.entity.ProductUnitEntity>
     **/
    @Override
    public List<ProductUnitEntity> listProductUnit(){
        LambdaQueryWrapper<ProductUnitEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductUnitEntity::getIsDeleted, Boolean.FALSE);
        return this.list(queryWrapper);
    }

    /**
     * @Description 删除产品单位
     * @Author Luo_WG
     * @Date 2022/9/27 15:02
     * @param id:主键id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean delete(String id){
        ProductUnitEntity entity = this.getById(id);
        if (entity.getOccupyStatus()) {
            throw new ServiceException(ApiError.ERROR_95168);
        }
        LambdaQueryWrapper<ProductUnitEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductUnitEntity::getId, id);
        return this.remove(queryWrapper);
    }

    /**
     * @Description 查询单位名称是否存在
     * @Author Luo_WG
     * @Date 2022/9/27 15:02
     * @param name 单位名称
     * @return java.lang.Boolean
     **/
    @Override
    public ProductUnitEntity checkUnitName(String name){
        LambdaQueryWrapper<ProductUnitEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductUnitEntity::getName, name);
        queryWrapper.eq(ProductUnitEntity::getIsDeleted, Boolean.FALSE);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 设置占用
     * @Author Luo_WG
     * @Date 2023/6/14 11:36
     * @param ids
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean setupOccupy(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().set(ProductUnitEntity::getOccupyStatus, Boolean.TRUE).in(ProductUnitEntity::getId, ids).update();
    }

    @Override
    public ProductUnitEntity getByName(String name) {
        ProductUnitEntity entity = lambdaQuery().eq(ProductUnitEntity::getName, name)
                .eq(ProductUnitEntity::getIsDeleted, Boolean.FALSE)
                .last("limit 1")
                .one();
        return entity;
    }
}




