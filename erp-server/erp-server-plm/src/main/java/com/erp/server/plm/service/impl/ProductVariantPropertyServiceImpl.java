package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.erp.model.plm.dto.ProductVariantDTO;
import com.erp.model.plm.dto.ProductVariantPropertyDTO;
import com.erp.model.plm.entity.ProductVariantEntity;
import com.erp.model.plm.entity.ProductVariantOptionEntity;
import com.erp.model.plm.entity.ProductVariantPropertyEntity;
import com.erp.server.plm.mapper.ProductVariantPropertyMapper;
import com.erp.server.plm.service.ProductVariantPropertyService;
import com.erp.server.plm.service.ProductVariantService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 产品变体值服务类
 * @Author Luo_WG
 * @Date 2022/9/26 11:19
 **/
@Service
public class ProductVariantPropertyServiceImpl extends ServiceImpl<ProductVariantPropertyMapper, ProductVariantPropertyEntity>
        implements ProductVariantPropertyService {

    @Resource
    private ProductVariantService productVariantService;

    /**
     * @Description 产品变体值查询列表
     * @Author Luo_WG
     * @Date 2022/9/26 14:06
     * @param variantId:产品变体类型表id
     * @return java.util.List<com.erp.model.plm.dto.ProductVariantPropertyEntity>
     **/
    @Override
    public List<ProductVariantPropertyEntity> list(String variantId) {
        LambdaQueryWrapper<ProductVariantPropertyEntity> queryWrapper = new LambdaQueryWrapper<>();
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
        ProductVariantPropertyEntity entity = this.getById(variantId);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95169);
        }
        if (entity.getOccupyStatus()) {
            throw new ServiceException(ApiError.ERROR_95168);
        }
        LambdaQueryWrapper<ProductVariantPropertyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductVariantPropertyEntity::getId, variantId);
        return this.remove(queryWrapper);
    }

    /**
     * 设置占用
     * @Author Luo_WG
     * @Date 2023/6/14 11:36
     * @param propertyValueList
     * @param propertyTypeList
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean setupOccupy(List<String> propertyValueList, List<String> propertyTypeList) {
        if (CollectionUtils.isEmpty(propertyValueList)) {
            return Boolean.TRUE;
        }
        List<ProductVariantDTO> productVariantDTOS = productVariantService.listVariantAndProperty();
        List<ProductVariantDTO> productVariantDTOList = productVariantDTOS.stream().filter(req -> propertyTypeList.contains(req.getPropertyType())).collect(Collectors.toList());
        List<String> propertyTypeIds = productVariantDTOList.stream().map(ProductVariantDTO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(propertyTypeIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate()
                .set(ProductVariantPropertyEntity::getOccupyStatus, Boolean.TRUE)
                .in(ProductVariantPropertyEntity::getPropertyValue, propertyValueList)
                .in(ProductVariantPropertyEntity::getVariantId, propertyTypeIds)
                .update();
    }
}




