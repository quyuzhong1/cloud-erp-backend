package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.server.scm.mapper.ProductInfoMapper;
import com.erp.server.scm.service.ProductInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfoEntity> implements ProductInfoService {

    /**
     * 根据主键Id查询产品表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:25
     * @param ids
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    @Override
    public List<ProductInfoEntity> ListProductInfoByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return baseMapper.ListProductInfoByIds(ids);
    }

    /**
     * 更新PLM同步过来的数据
     * @Author Luo_WG
     * @Date 2023/4/19 16:05
     **/
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateProductInfo(List<ProductInfoEntity> productInfoEntityList) {
        List<String> detailIds = productInfoEntityList.stream().map(ProductInfoEntity::getId).collect(Collectors.toList());
        List<ProductInfoEntity> detailEntityList = ListProductInfoByIds(detailIds);
        List<String> ids = productInfoEntityList.stream().map(ProductInfoEntity::getId).collect(Collectors.toList());
        List<String> dbIds = detailEntityList.stream().map(ProductInfoEntity::getId).collect(Collectors.toList());
        List<String> existIdList = ids.stream().filter(s -> dbIds.contains(s)).collect(Collectors.toList());
        List<String> notExistIdList = ids.stream().filter(s -> !dbIds.contains(s)).collect(Collectors.toList());
        List<ProductInfoEntity> notExistDetailEntityList = new ArrayList<>();
        List<ProductInfoEntity> existDetailEntityList = new ArrayList<>();
        for (ProductInfoEntity detailEntity : productInfoEntityList) {
            if (notExistIdList.contains(detailEntity.getId())) {
//                notExistDetailEntityList.add(detailEntity);
                this.save(detailEntity);
            }
            if (existIdList.contains(detailEntity.getId())) {
//                existDetailEntityList.add(detailEntity);
                baseMapper.updateByPrimaryKeySelective(detailEntity);
            }
        }
//        if (CollectionUtils.isNotEmpty(notExistDetailEntityList)) {
//            this.saveBatch(notExistDetailEntityList);
//        }
//        if (CollectionUtils.isNotEmpty(existDetailEntityList)) {
//            baseMapper.updateBatchSelective(existDetailEntityList);
//        }
        return Boolean.TRUE;
    }
}
