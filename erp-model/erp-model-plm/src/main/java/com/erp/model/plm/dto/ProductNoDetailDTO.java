package com.erp.model.plm.dto;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "产品信息列表id")
    private String id;

    @ApiModelProperty(value = "产品图片")
    private String imagesUrl;

    @ApiModelProperty(value = "sku")
    private String sku;

    @ApiModelProperty(value = "产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发")
    private String productState;

    @ApiModelProperty(value = "产品名称")
    private String name;

    @ApiModelProperty(value = "产品经理")
    private String chargeName;

    @ApiModelProperty(value = "产品经理Id")
    private String chargeId;

    @ApiModelProperty(value = "品牌")
    private String brandName;

    @ApiModelProperty(value = "品牌id")
    private String brandId;

    @ApiModelProperty(value = "产品属性")
    private String property;

    @ApiModelProperty(value = "产品等级")
    private String grade;

    @ApiModelProperty(value = "产品类别")
    private String category;

    @ApiModelProperty(value = "销售方式")
    private String saleMethod;

    @ApiModelProperty(value = "产品卖点")
    private String productSellSpot;

    @ApiModelProperty(value = "产品功能描述")
    private String productFunctionDesc;

    @ApiModelProperty(value = "产品用途")
    private String usageDesc;

    @ApiModelProperty(value = "存在侵权风险 1：有侵权风险 2：无侵权风险")
    private String pirateRisk;

    @ApiModelProperty(value = "主要材质")
    private String materials;

    @ApiModelProperty(value = "计划上市时间")
    private String planListedTime;

    @ApiModelProperty(value = "单位表id")
    private String unitId;
}
