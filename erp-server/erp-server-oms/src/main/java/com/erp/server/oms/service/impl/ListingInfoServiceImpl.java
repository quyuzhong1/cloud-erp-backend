package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.enums.TypeEnum;
import com.erp.server.oms.mapper.ListingInfoMapper;
import com.erp.server.oms.service.ListingInfoService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 对应平台sku 表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-18
 */
@Service
public class ListingInfoServiceImpl extends SuperServiceImpl<ListingInfoMapper, ListingInfoEntity> implements ListingInfoService {


    /**
     * 根据 sku 获取到listing 数据
     *
     * @param skuNo
     * @return com.erp.model.oms.entity.ListingInfoEntity
     * @author yl
     * @date 2023-08-18 16:35
     */
    @Override
    public ListingInfoEntity getBySkuNo(String skuNo, String type) {
        return lambdaQuery().eq(ListingInfoEntity::getSkuNo, skuNo).
                eq(ListingInfoEntity::getType, type).
                last("LIMIT 1").
                one();
    }


    /**
     * 添加库存sku
     *
     * @param skuNo
     * @param productName
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addWarehouseSku(String skuNo, String productName) {
        String id = IdWorker.getIdStr();
        ListingInfoEntity listingInfoEntity = new ListingInfoEntity();
        listingInfoEntity.setId(id);
        listingInfoEntity.setSkuNo(skuNo);
        listingInfoEntity.setProductName(productName);
        listingInfoEntity.setType(TypeEnum.WAREHOUSE.getCode());
        if (this.save(listingInfoEntity)) {
            return id;
        } else {
            return "";
        }
    }
}
