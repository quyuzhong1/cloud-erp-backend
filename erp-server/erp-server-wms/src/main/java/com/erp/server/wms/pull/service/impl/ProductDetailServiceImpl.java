package com.erp.server.wms.pull.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.wms.pull.mapper.ProductDetailMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductDetailServiceImpl extends ServiceImpl<ProductDetailMapper, ProductDetailEntity> implements ProductDetailService {

    /**
     * 更新PLM同步过来的数据
     * @Author zhangchunlin
     * @Date 2023-05-11 18:10
     **/
/*    public void saveOrUpdateProductDetail(List<ProductDetailEntity> productDetailEntities) {
        for(ProductDetailEntity productDetailEntity: productDetailEntities) {
            log.info("开始同步SKU编号：【{}】", productDetailEntity.getSkuNo());
            ProductDetailEntity entity = this.baseMapper.getProductDetailById(productDetailEntity.getId());
            //不存在需要新增，同时判断产品是否更新，用最后更新时间
            if (ObjectUtil.isNotEmpty(entity)) {
                if (!Objects.equals(entity.getUpdateTime(), productDetailEntity.getUpdateTime())) {
                    try {
                        this.baseMapper.updateAllById(productDetailEntity);
                    } catch (Exception e) {
                        log.error("SKU信息【{}】更新异常", productDetailEntity.getSkuNo(), e);
                    }
                } else {
                    // 数据没有发生变更
                }
            } else {
                try {
                    this.save(productDetailEntity);
                } catch (Exception e) {
                    log.error("SKU信息【{}】新增异常", productDetailEntity.getSkuNo(), e);
                }
            }
        }
    }*/

    /**
     * 根据主键Id查询产品Sku表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:25
     * @param ids
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    @Override
    public List<ProductDetailEntity> listProductDetailByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return FeignQuery.getByIds(ProductDetailEntity.class, ids);
    }

    /**
     * 更新PLM同步过来的数据
     * @Author Luo_WG
     * @Date 2023/4/19 16:05
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateProductDetail(List<ProductDetailEntity> productDetailEntityList) {
        List<String> detailIds = productDetailEntityList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
        List<ProductDetailEntity> detailEntityList = listProductDetailByIds(detailIds);
        List<String> ids = productDetailEntityList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
        List<String> dbIds = detailEntityList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
        List<String> existIdList = ids.stream().filter(s -> dbIds.contains(s)).collect(Collectors.toList());
        List<String> notExistIdList = ids.stream().filter(s -> !dbIds.contains(s)).collect(Collectors.toList());
        for (ProductDetailEntity detailEntity : productDetailEntityList) {
            if (notExistIdList.contains(detailEntity.getId())) {
                this.save(detailEntity);
            }
            if (existIdList.contains(detailEntity.getId())) {
                baseMapper.updateAllById(detailEntity);
            }
        }

        return Boolean.TRUE;
    }
}
