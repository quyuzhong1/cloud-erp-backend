package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.ProductImagesDTO;
import com.erp.model.plm.entity.ProductImagesEntity;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProductImagesMapper;
import com.erp.server.plm.service.ProductImagesService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Description 产品图片服务类
 * @Author Luo_WG
 * @Date 2022/9/26 16:30
 **/
@Service
public class ProductImagesServiceImpl extends ServiceImpl<ProductImagesMapper, ProductImagesEntity>
    implements ProductImagesService {

    /**
     * @param productImagesDTO 产品图片信息请求参数
     * @return java.lang.Boolean
     * @Description 新增产品图片
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     **/
    @Override
    public Boolean insertProductImage(ProductImagesDTO productImagesDTO) {
        ProductImagesEntity productImagesEntity = new ProductImagesEntity();
        BeanMapper.copy(productImagesDTO, productImagesEntity);
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            if (StringUtils.isBlank(productImagesDTO.getId())) {
                productImagesEntity.setCreateUserId(loginUser.getUid());
                productImagesEntity.setCreateUserName(loginUser.getUserName());
            } else {
                productImagesEntity.setUpdateUserId(loginUser.getUid());
                productImagesEntity.setUpdateUserName(loginUser.getUserName());
            }
        }
        return this.save(productImagesEntity);
    }

    /**
     * @Description 修改产品图片
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productImagesDTO 产品图片信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateProductImage(ProductImagesDTO productImagesDTO) {
        LambdaUpdateWrapper<ProductImagesEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper();
        lambdaUpdateWrapper.set(ProductImagesEntity::getImagesUrl, productImagesDTO.getImagesUrl());
        lambdaUpdateWrapper.eq(ProductImagesEntity::getSkuId, productImagesDTO.getSkuId());
        lambdaUpdateWrapper.eq(ProductImagesEntity::getProductId, productImagesDTO.getProductId());
        return this.update(lambdaUpdateWrapper);
    }


}




