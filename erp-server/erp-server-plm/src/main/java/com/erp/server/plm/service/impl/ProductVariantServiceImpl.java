package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.ProductVariantDTO;
import com.erp.model.plm.entity.ProductVariantEntity;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProductVariantMapper;
import com.erp.server.plm.service.ProductVariantService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 产品变体信息服务类
 * @Author Luo_WG
 * @Date 2022/9/26 11:19
 **/
@Service
public class ProductVariantServiceImpl extends ServiceImpl<ProductVariantMapper, ProductVariantEntity>
    implements ProductVariantService {

    /**
     * @Description 产品变体类型查询列表
     * @Author Luo_WG
     * @Date 2022/9/26 14:06
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductVariantEntity>
     **/
    @Override
    public List<ProductVariantEntity> list(String productId) {
        LambdaQueryWrapper<ProductVariantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductVariantEntity::getProductId, productId);
        return this.list(queryWrapper);
    }

    /**
     * @Description 保存/修改产品变体类型信息
     * @Author Luo_WG
     * @Date 2022/9/26 10:13
     * @param productVariantDTO 产品变体类型信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdate(ProductVariantDTO productVariantDTO) {
        ProductVariantEntity variantEntity = new ProductVariantEntity();
        BeanMapper.copy(productVariantDTO, variantEntity);
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (StringUtils.isBlank(productVariantDTO.getId())) {
            variantEntity.setCreateUserId(loginUser.getUid());
            variantEntity.setCreateUserName(loginUser.getUserName());
        } else {
            variantEntity.setUpdateUserId(loginUser.getUid());
            variantEntity.setUpdateUserName(loginUser.getUserName());
        }
        return this.saveOrUpdate(variantEntity);
    }

    /**
     * @Description 删除产品变体类型值信息
     * @Author Luo_WG
     * @Date 2022/9/26 16:13
     * @param variantId:变体类型表主键Id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean deleteVariant(String variantId) {
        LambdaQueryWrapper<ProductVariantEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductVariantEntity::getId, variantId);
        return this.remove(queryWrapper);
    }
}




