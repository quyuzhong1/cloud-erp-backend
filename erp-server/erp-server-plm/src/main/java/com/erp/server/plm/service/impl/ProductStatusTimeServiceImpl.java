package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductStatusTimeEntity;
import com.erp.server.plm.mapper.ProductStatusTimeMapper;
import com.erp.server.plm.service.ProductStatusTimeService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:56
 */
@Service
public class ProductStatusTimeServiceImpl extends ServiceImpl<ProductStatusTimeMapper, ProductStatusTimeEntity>
        implements ProductStatusTimeService {

    @Override
    public void saveOrUpdateProductStatusTime(String productId, Integer approvalStatus) {
        ProductStatusTimeEntity productStatusTimeEntity = getByProductIdAndStatus(productId, approvalStatus);
        if (ObjectUtils.isNotEmpty(productStatusTimeEntity)) {
            //存在则更新状态时间
            productStatusTimeEntity.setStatusTime(LocalDateTime.now());
        } else {
            //不存在则新增
            productStatusTimeEntity = new ProductStatusTimeEntity();
            productStatusTimeEntity.setProductId(productId);
            productStatusTimeEntity.setStatusTime(LocalDateTime.now());
            productStatusTimeEntity.setStatus(String.valueOf(approvalStatus));
        }
        this.saveOrUpdate(productStatusTimeEntity);
    }

    @Override
    public List<ProductStatusTimeEntity> listByProductId(String productId) {
        LambdaQueryWrapper<ProductStatusTimeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductStatusTimeEntity::getProductId, productId);
        return this.list(queryWrapper);
    }


    /**
     * 批量修改或者添加对应的状态及时间
     *
     * @param productIdList
     * @param approvalCode
     * @return void
     * @author yl
     * @date 2023-06-14 16:52
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveOrUpdateProductStatusTime(List<String> productIdList, Integer approvalCode) {
        if (CollectionUtils.isEmpty(productIdList)) {
            return;
        }
        List<ProductStatusTimeEntity> dbList = this.listByProductIdsIdAndStatus(productIdList, approvalCode);
        List<ProductStatusTimeEntity> saveOrUpdateList = new ArrayList<>(productIdList.size());
        LocalDateTime now = LocalDateTime.now();
        for (String productId : productIdList) {
            ProductStatusTimeEntity update = dbList.stream().filter(p -> p.getProductId().equals(productId)).findFirst().orElse(null);
            if (update != null) {
                update.setStatusTime(now);
                saveOrUpdateList.add(update);
            } else {
                //不存在则新增
                ProductStatusTimeEntity save = new ProductStatusTimeEntity();
                save.setProductId(productId);
                save.setStatusTime(LocalDateTime.now());
                save.setStatus(String.valueOf(approvalCode));
                saveOrUpdateList.add(save);
            }
        }

        if (CollectionUtils.isNotEmpty(saveOrUpdateList)) {
            this.saveOrUpdateBatch(saveOrUpdateList);
        }


    }

    /**
     * @param productId
     * @param approvalStatus
     * @return ProductStatusTimeEntity
     * @description: 根据产品id和状态查询
     * @author Will
     * @date: 2023/2/24 10:57
     */
    private ProductStatusTimeEntity getByProductIdAndStatus(String productId, Integer approvalStatus) {
        LambdaQueryWrapper<ProductStatusTimeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductStatusTimeEntity::getProductId, productId);
        queryWrapper.eq(ProductStatusTimeEntity::getStatus, String.valueOf(approvalStatus));
        return this.getOne(queryWrapper);
    }


    private List<ProductStatusTimeEntity> listByProductIdsIdAndStatus(List<String> productIdList, Integer approvalStatus) {
        LambdaQueryWrapper<ProductStatusTimeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductStatusTimeEntity::getProductId, productIdList);
        queryWrapper.eq(ProductStatusTimeEntity::getStatus, String.valueOf(approvalStatus));
        return this.list(queryWrapper);
    }

}
