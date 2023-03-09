package com.erp.server.plm.service;

import com.erp.model.plm.enums.BusinessNoTypeEnum;

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
    /**
     * @description: 生成spuNo
     * @author Will
     * @date: 2023/1/7 9:14
     * @param categoryId
     * @return String
     */
    String getSpuNo(String categoryId);

    /**
     * @description: 根据编号头和编号类型生成编号
     * @author Will
     * @date: 2023/3/9 10:27
     * @param businesshead
     * @param businessNoTypeEnum
     * @return String
     */
    String getBusinessNo(String businesshead, BusinessNoTypeEnum businessNoTypeEnum);
}
