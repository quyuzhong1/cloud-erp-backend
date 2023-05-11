package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.ProductPurchaseDTO;
import com.erp.model.plm.dto.ProductPurchaseShowDTO;
import com.erp.model.plm.entity.ProductPurchaseEntity;
import com.erp.server.plm.mapper.ProductPurchaseMapper;
import com.erp.server.plm.service.ProductPurchaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description 产品采购信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 11:23
 **/
@Service
public class ProductPurchaseServiceImpl extends ServiceImpl<ProductPurchaseMapper, ProductPurchaseEntity> implements ProductPurchaseService {

    @Resource
    private ProductPurchaseMapper productPurchaseMapper;

    /**
     * @Description 产品采购信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 11:48
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductPurchaseShowDTO>
     **/
    @Override
    public List<ProductPurchaseShowDTO> list(String productId) {
        return productPurchaseMapper.list(productId);
    }

    /**
    * @Description 保存/修改产品采购信息
    * @Author Luo_WG
    * @Date 2022/9/23 11:48
    * @param purchaseDTO 产品采购信息表请求参数
    * @return java.lang.String
    **/
    @Override
    public Boolean saveOrUpdate(ProductPurchaseDTO purchaseDTO) {
        ProductPurchaseEntity purchaseEntity = new ProductPurchaseEntity();
        BeanMapper.copy(purchaseDTO, purchaseEntity);
        //验证数据
        checkProductPurchase(purchaseEntity);
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            if (StringUtils.isBlank(purchaseDTO.getId())) {
                purchaseEntity.setCreateUserId(loginUser.getUid());
                purchaseEntity.setCreateUserName(loginUser.getUserName());
            } else {
                purchaseEntity.setUpdateUserId(loginUser.getUid());
                purchaseEntity.setUpdateUserName(loginUser.getUserName());
            }
        }
        return this.saveOrUpdate(purchaseEntity);
    }

    /**
     * @Description 保存/修改产品采购信息-批量
     * @Author Luo_WG
     * @Date 2022/9/26 18:05
     * @param purchaseList 产品成本信息表
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductPurchaseDTO> purchaseList) {
        List<ProductPurchaseEntity> list = BeanMapper.copyList(purchaseList, ProductPurchaseEntity.class);

        Map<String, List<ProductPurchaseEntity>> map = list.stream().filter(obj -> StringUtils.isNotBlank(obj.getEan())).collect(Collectors.groupingBy(ProductPurchaseEntity::getEan));
        for (Map.Entry<String, List<ProductPurchaseEntity>> entry : map.entrySet()) {
            String ean = entry.getKey();
            List<ProductPurchaseEntity> eanList = entry.getValue();
            if (StringUtils.isNotBlank(ean) && eanList.size() > 1) {
                throw new ServiceException(new ApiResult(1,"不可新增相同EAN码"));
            }
            //验证数据
            checkProductPurchase(eanList.get(0));
        }
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
    public Boolean removePurchase(String skuId) {
        LambdaQueryWrapper<ProductPurchaseEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductPurchaseEntity::getSkuId, skuId);
        return this.remove(queryWrapper);
    }

    @Override
    public ProductPurchaseEntity getBySkuId(String skuId) {
        LambdaQueryWrapper<ProductPurchaseEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductPurchaseEntity::getSkuId, skuId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    /**
     * @description: 数据验证
     * @author Will
     * @date: 2023/5/11 16:20
     * @param entity
     */
    private void checkProductPurchase (ProductPurchaseEntity entity) {
        //ean不能重复
        String ean = entity.getEan();
        if (StringUtils.isBlank(ean)) {
            return;
        }
        List<ProductPurchaseEntity> list = lambdaQuery().eq(ProductPurchaseEntity::getEan, ean).list();
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> ids = list.stream().map(ProductPurchaseEntity::getId).collect(Collectors.toList());
            if (ids.size() > 1 || !ids.contains(entity.getId())) {
                throw new ServiceException(new ApiResult(1,"EAN码已存在，不可重复新增"));
            }
        }
    }

}




