package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname SkuVO

 * @Date 2023-01-11 14:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SkuSimpleVO implements Serializable {


    private String skuId;

    private String productId;
    /**
     * sku no
     */
    private String skuNo;

    /**
     * 是否是捆绑商品:true=是，false=否
     * (可能字段为null，需添加查询)
     */
    private Boolean isCombination;
}
