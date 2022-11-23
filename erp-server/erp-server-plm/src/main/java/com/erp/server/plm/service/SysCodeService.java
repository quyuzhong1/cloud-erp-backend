package com.erp.server.plm.service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/23 19:14
 */
public interface SysCodeService {

    /**
     * @description: 生成skuNo
     * @author Will
     * @date: 2022/11/22 14:19
     * @param productId
     * @param variantColorProperty
     * @return String
     */
    String getSkuNo(String productId,String variantColorProperty);
}
