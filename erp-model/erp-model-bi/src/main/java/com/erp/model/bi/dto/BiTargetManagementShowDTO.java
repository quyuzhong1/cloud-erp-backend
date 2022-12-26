package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/21 17:41
 */
@Data
@NoArgsConstructor
public class BiTargetManagementShowDTO {

    /**
     * 主键id
     */
    private String id;
    /**
     * 年份
     */
    private String year;
    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 产品类别
     */
    private String category;

    /**
     * 目标类型（0销量，1销售额）
     */
    private Integer targetType;

    /**
     * 销售类型名称
     */
    private String targetTypeName;

    /**
     * 产品类型（0新品，1老品）
     */
    private Integer productType;

    /**
     * 产品类型名称
     */
    private String productTypeName;

    /**
     * 产品定位
     */
    private String productPosition;

    /**
     * sku
     */
    private String skuNo;

    /**
     * spu
     */
    private String spuNo;

    /**
     * 品名
     */
    private String productName;

    /**
     * 客单价
     */
    private BigDecimal salePrice;

    /**
     * 一月
     */
    private BigDecimal january;

    /**
     * 二月
     */
    private BigDecimal february;


    /**
     * 三月
     */
    private BigDecimal march;


    /**
     * 四月
     */
    private BigDecimal april;

    /**
     * 五月
     */
    private BigDecimal may;

    /**
     * 六月
     */
    private BigDecimal june;

    /**
     * 七月
     */
    private BigDecimal july;

    /**
     * 八月
     */
    private BigDecimal august;

    /**
     * 九月
     */
    private BigDecimal september;

    /**
     * 十月
     */
    private BigDecimal october;

    /**
     * 十一月
     */
    private BigDecimal november;

    /**
     * 十二月
     */
    private BigDecimal december;

}
