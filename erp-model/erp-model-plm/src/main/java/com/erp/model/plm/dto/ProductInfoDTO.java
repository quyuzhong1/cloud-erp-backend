package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Description 产品基础信息请求参数
 * @Author Luo_WG
 * @Date 2022/9/23 17:26
 **/
@Data
@NoArgsConstructor
public class ProductInfoDTO {
    /**
     * 主键id
     */
    private String id;

    /**
     * 产品名
     */
    @NotBlank(message = "产品名不能为空")
    private String name;

    /**
     * 产品类别
     */
    private String category;

    /**
     * 产品属性
     */
    private String property;

    /**
     * 产品属性id
     */
    private String propertyId;

    /**
     * 产品负责人
     */
    @NotBlank(message = "产品负责人不能为空")
    private String chargeName;

    /**
     * 负责人id
     */
    @NotBlank(message = "负责人id不能为空")
    private String chargeId;

    /**
     * 产品等级
     */
    private String grade;

    /**
     * 产品品牌
     */
    private String brandName;

    /**
     * 品牌id
     */
    private String brandId;

    /**
     * 分类id
     */
    private String categoryId;

    /**
     * spuNo
     */
    private String spuNo;

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
    private Integer pirateRisk;

    /**
     * 主要材质
     */
    private String materials;

    /**
     * 规格类型  1：无规格  2：多规格
     */
    @StateEnumValue(intValues = {1, 2}, message = "规格类型1或者2")
    private Integer specType;

    /**
     * 销售方式
     */
    @StateEnumValue(intValues = {1, 2, 3}, message = "销售方式1或者2或者3")
    private String saleMethod;

    private Integer approvalStatus;
}