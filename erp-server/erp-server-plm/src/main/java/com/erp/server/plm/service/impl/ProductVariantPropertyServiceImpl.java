package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.ProductVariantPropertyDTO;
import com.erp.model.plm.entity.ProductVariantPropertyEntity;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProductVariantPropertyMapper;
import com.erp.server.plm.service.ProductVariantPropertyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * @Description 产品变体值服务类
 * @Author Luo_WG
 * @Date 2022/9/26 11:19
 **/
@Service
public class ProductVariantPropertyServiceImpl extends ServiceImpl<ProductVariantPropertyMapper, ProductVariantPropertyEntity>
    implements ProductVariantPropertyService {

    /**
     * @Description 产品变体值查询列表
     * @Author Luo_WG
     * @Date 2022/9/26 14:06
     * @param variantId:产品变体类型表id
     * @return java.util.List<com.erp.model.plm.dto.ProductVariantPropertyEntity>
     **/
    @Override
    public List<ProductVariantPropertyEntity> list(String variantId) {
        LambdaQueryWrapper<ProductVariantPropertyEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductVariantPropertyEntity::getVariantId, variantId);
        return this.list(queryWrapper);
    }

    /**
     * @Description 保存/修改产品变体类型值信息
     * @Author Luo_WG
     * @Date 2022/9/26 10:13
     * @param dto 产品变体值信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdate(ProductVariantPropertyDTO dto) {
        ProductVariantPropertyEntity variantPropertyEntity = new ProductVariantPropertyEntity();
        BeanMapper.copy(dto, variantPropertyEntity);
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            if (StringUtils.isBlank(dto.getId())) {
                variantPropertyEntity.setCreateUserId(loginUser.getUid());
                variantPropertyEntity.setCreateUserName(loginUser.getUserName());
            } else {
                variantPropertyEntity.setUpdateUserId(loginUser.getUid());
                variantPropertyEntity.setUpdateUserName(loginUser.getUserName());
            }
        }
        return this.saveOrUpdate(variantPropertyEntity);
    }

    /**
     * @Description 保存/修改产品变体类型值信息-批量
     * @Author Luo_WG
     * @Date 2022/9/26 10:13
     * @param dto 产品变体值信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductVariantPropertyDTO> dto) {
        List<ProductVariantPropertyEntity> variantPropertyEntityList = BeanMapper.copyList(dto, ProductVariantPropertyEntity.class);
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            variantPropertyEntityList.forEach(req -> {
                if (StringUtils.isBlank(req.getId())) {
                    req.setCreateUserId(loginUser.getUid());
                    req.setCreateUserName(loginUser.getUserName());
                } else {
                    req.setUpdateUserId(loginUser.getUid());
                    req.setUpdateUserName(loginUser.getUserName());
                }
            });
        }
        return this.saveOrUpdateBatch(variantPropertyEntityList);
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
        LambdaQueryWrapper<ProductVariantPropertyEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductVariantPropertyEntity::getId, variantId);
        return this.remove(queryWrapper);
    }
}




