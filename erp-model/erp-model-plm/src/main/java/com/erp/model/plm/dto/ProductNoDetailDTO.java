package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
* @Description 查询无规格明细信息实体类（VO）
* @Author Luo_WG
* @Date 2022/9/22 14:46
**/
@Data
@NoArgsConstructor
public class ProductNoDetailDTO {
    /**
     * 产品信息列表id
     */
    private String id;

    /**
     * 产品图片
     */
    private String imagesUrl;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    private String productState;

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
     * 品牌id
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
     * 销售方式
     */
    private String saleMethod;

    /**
     * 产品卖点
     */
    private String productSellSpot;

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
     * 计划上市时间
     */
    private String planListedTime;

    /**
     * 单位表id
     */
    private String unitId;
}
