package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.ProductCostDTO;
import com.erp.model.plm.dto.ProductCostShowDTO;
import com.erp.model.plm.entity.ProductCostEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPurchaseEntity;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProductCostMapper;
import com.erp.server.plm.service.ProductCostService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 产品成本信息服务类
 * @Author Luo_WG
 * @Date 2022/9/22 16:15
 **/
@Service
public class ProductCostServiceImpl extends ServiceImpl<ProductCostMapper, ProductCostEntity> implements ProductCostService {

    @Resource
    private ProductCostMapper productCostMapper;

    /**
     * @Description 产品成本信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>
     **/
    @Override
    public List<ProductCostShowDTO> list(String productId) {
        return productCostMapper.list(productId);
    }

    /**
     * @Description 保存/修改产品成本信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productCostDTO 产品成本信息表
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdate(ProductCostDTO productCostDTO) {
        ProductCostEntity costEntity = new ProductCostEntity();
        BeanMapper.copy(productCostDTO, costEntity);
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            if (StringUtils.isBlank(productCostDTO.getId())) {
                costEntity.setCreateUserId(loginUser.getUid());
                costEntity.setCreateUserName(loginUser.getUserName());
            } else {
                costEntity.setUpdateUserId(loginUser.getUid());
                costEntity.setUpdateUserName(loginUser.getUserName());
            }
        }
        return this.saveOrUpdate(costEntity);
    }

    /**
     * @Description 保存/修改产品成本信息-批量
     * @Author Luo_WG
     * @Date 2022/9/26 18:05
     * @param productCostList 产品成本信息表
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductCostDTO> productCostList) {
        List<ProductCostEntity> list = BeanMapper.copyList(productCostList, ProductCostEntity.class);
        return this.saveOrUpdateBatch(list);
    }

    /**
     * @Description 删除产品采购信息
     * @Author Luo_WG
     * @Date 2022/9/26 18:42
     * @param skuId 产品sku明细表id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean removeCost(String skuId) {
        LambdaQueryWrapper<ProductCostEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductCostEntity::getSkuId, skuId);
        return this.remove(queryWrapper);
    }
}




