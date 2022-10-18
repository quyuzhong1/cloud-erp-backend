package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProductManySpecBaseDTO {

    /**
     * 产品信息列表id
     */
    private String id;

    /**
     * spuNo
     */
    private String spuNo;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品经理
     */
    private String chargeName;

    /**
     * 产品经理Id
     */
    private String chargeId;

    /**
     * 品牌
     */
    private String brandName;

    /**
     * 品牌Id
     */
    private String brandId;

    /**
     * 产品属性
     */
    private String property;

    /**
     * 产品属性Id
     */
    private String propertyId;

    /**
     * 产品等级
     */
    private String grade;

    /**
     * 产品类别
     */
    private String category;

    /**
     * 产品类别Id
     */
    private String categoryId;

    /**
     * 销售方式
     */
    private String saleMethod;

    /**
     * 产品卖点
     */
    private String sellSpot;

    /**
     * 产品功能描述
     */
    private String functionDesc;

    /**
     * 产品用途
     */
    private String usageDesc;

    /**
     * 存在侵权风险 1：有侵权风险 2：无侵权风险
     */
    private String pirateRisk;

    /**
     * 主要材质
     */
    private String materials;

    /**
     * 产品分类id集合
     */
    private List<String> categoryIdList;
}
