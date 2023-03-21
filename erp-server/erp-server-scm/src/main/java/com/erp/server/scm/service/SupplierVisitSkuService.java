package com.erp.server.scm.service;

import com.common.core.serveice.SuperService;
import com.erp.model.scm.entity.SupplierVisitSkuEntity;

import java.util.List;

/**
 * <p>
 * 供应商拜访物料信息表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
public interface SupplierVisitSkuService extends SuperService<SupplierVisitSkuEntity> {

    
    /**
     * 获取到skuId集合
     * @author yl
     * @date 2023-03-21 11:51
     * @param ids
     * @return java.util.List<java.lang.String>
     */
    List<String> getByVisitIds(List<String> ids);
}
