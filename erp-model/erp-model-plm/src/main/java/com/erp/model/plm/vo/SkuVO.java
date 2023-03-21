package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname SkuVO
 * @Description TODO
 * @Date 2023-01-11 14:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SkuVO implements Serializable {


    private String skuId;

    private String productId;

    /**
     * sku no
     */
    private String skuNo;

    private String skuImagesUrl;

    /**
     * 单箱数量
     */
    private Integer unitQty;

    /**
     * sku 名称
     */
    private String skuName = "";


    /**
     * spu no
     */
    private String spuNo = "";


    /**
     * spu 名称
     */
    private String spuName = "";
}
