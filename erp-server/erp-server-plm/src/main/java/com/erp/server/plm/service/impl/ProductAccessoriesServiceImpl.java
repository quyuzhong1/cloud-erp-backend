package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.ProductAccessoriesDTO;
import com.erp.model.plm.entity.ProductAccessoriesEntity;
import com.erp.server.plm.mapper.ProductAccessoriesMapper;
import com.erp.server.plm.service.ProductAccessoriesService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品包装/辅料信息(ProductAccessories)表服务实现类
 *
 * @author yl
 * @since 2023-02-25 12:58:11
 */
@Service
public class ProductAccessoriesServiceImpl extends ServiceImpl<ProductAccessoriesMapper, ProductAccessoriesEntity> implements ProductAccessoriesService {


    /**
     * 批量保存或者修改包装辅料的信息
     *
     * @param productAccessoriesList
     */
    @Override
    public Boolean saveOrUpdateBatchAccessories(List<ProductAccessoriesDTO> productAccessoriesList) {
        if (CollectionUtils.isNotEmpty(productAccessoriesList)) {
            List<ProductAccessoriesEntity> list = BeanMapper.copyList(productAccessoriesList, ProductAccessoriesEntity.class);
            return this.saveOrUpdateBatch(list);
        }
        return true;
    }

    /**
     * 根据产品id 获取辅料信息
     *
     * @param productId
     * @return
     */
    @Override
    public List<ProductAccessoriesDTO> getByProductId(String productId) {
        List<ProductAccessoriesDTO> resultList = baseMapper.getByProductId(productId);
        return resultList;
    }

    /**
     * 根据产品id 获取辅料信息
     *
     * @param skuId
     * @return
     */
    @Override
    public List<ProductAccessoriesDTO> getBySkuId(String skuId) {
        List<ProductAccessoriesDTO> resultList = baseMapper.getBySkuId(skuId);
        return resultList;
    }

    @Override
    public List<ProductAccessoriesEntity> getListByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ProductAccessoriesEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductAccessoriesEntity::getId, ids);
        return this.list(queryWrapper);
    }

    @Override
    public List<ProductAccessoriesDTO.ListDTO> listAccessories(ProductAccessoriesDTO.ParamDTO dto) {
        List<ProductAccessoriesEntity> list = lambdaQuery().in(ProductAccessoriesEntity::getParentSkuId, dto.getSkuIdList()).list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<ProductAccessoriesEntity> accessoriesList = list.stream().filter(obj -> StringUtils.isNotBlank(obj.getAccessoriesSkuId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(accessoriesList)) {
            return Collections.EMPTY_LIST;
        }
        List<ProductAccessoriesDTO.ListDTO> resultList = BeanMapperUtils.copyList(ProductAccessoriesDTO.ListDTO.class, list);
        return resultList;
    }


    public List<ProductAccessoriesEntity> listByProductId(String productId) {
        if (StringUtils.isBlank(productId)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ProductAccessoriesEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductAccessoriesEntity::getProductId, productId);
        return this.list(queryWrapper);
    }


}
