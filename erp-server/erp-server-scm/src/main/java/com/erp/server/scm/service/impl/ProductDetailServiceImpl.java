package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.scm.mapper.ProductDetailMapper;
import com.erp.server.scm.service.ProductDetailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductDetailServiceImpl extends ServiceImpl<ProductDetailMapper, ProductDetailEntity> implements ProductDetailService {

    /**
     * 根据主键Id查询产品Sku表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:25
     * @param ids
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    @Override
    public List<ProductDetailEntity> ListProductDetailByIds(List<String> ids) {
        return baseMapper.ListProductDetailByIds(ids);
    }

    /**
     * 更新PLM同步过来的数据
     * @Author Luo_WG
     * @Date 2023/4/19 16:05
     **/
    public Boolean saveOrUpdateProductDetail(List<ProductDetailEntity> productDetailEntityList) {
        List<String> detailIds = productDetailEntityList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
        List<ProductDetailEntity> detailEntityList = ListProductDetailByIds(detailIds);
        List<String> ids = productDetailEntityList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
        List<String> dbIds = detailEntityList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
        List<String> existIdList = ids.stream().filter(s -> dbIds.contains(s)).collect(Collectors.toList());
        List<String> notExistIdList = ids.stream().filter(s -> !dbIds.contains(s)).collect(Collectors.toList());
        List<ProductDetailEntity> existDetailEntityList = ListProductDetailByIds(existIdList);
        List<ProductDetailEntity> notExistDetailEntityList = ListProductDetailByIds(notExistIdList);
        baseMapper.updateBatchSelective(existDetailEntityList);
        this.saveBatch(notExistDetailEntityList);
        return Boolean.TRUE;
    }
}
