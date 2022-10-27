package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.ProductUnitDTO;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import com.erp.model.plm.entity.ProductUnitEntity;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProductUnitMapper;
import com.erp.server.plm.service.ProductUnitService;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            for (ProductUnitEntity productUnitEntity : productUnitEntities) {
                if (StringUtils.isBlank(productUnitEntity.getId())) {
                    productUnitEntity.setCreateUserId(loginUser.getUid());
                    productUnitEntity.setCreateUserName(loginUser.getUserName());
                } else {
                    productUnitEntity.setUpdateUserId(loginUser.getUid());
                    productUnitEntity.setUpdateUserName(loginUser.getUserName());
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
        return this.list();
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
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }
}




