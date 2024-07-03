package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.BiSkuInfoEntity;

/**
 * 商品信息服务类
 */
public interface DmpSkuInfoService extends IService<BiSkuInfoEntity> {
    /**
     * 根据sku查询商品信息
     *
     * @param skuNo     商品sku
     * @param companyId
     * @return com.erp.model.dmp.entity.DmpSkuInfoEntity
     **/
    BiSkuInfoEntity getBySkuNo(String skuNo, String companyId);
}
