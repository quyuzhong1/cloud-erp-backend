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

    /**
     * 报关型号
     */
    private String declareModel;

    /**
     * 报关名称
     */
    private String declareName;

    /**
     * 变体信息
     */
    private String variantProperty;

    /**
     * 主要材质
     */
    private String materials;

    /**
     * 功能描述
     */
    private String functionDesc;
    /**
     * 产品属性
     */
    private String productPropertyId;


    /**
     * 产品等级
     */
    private String productGrade;
}
