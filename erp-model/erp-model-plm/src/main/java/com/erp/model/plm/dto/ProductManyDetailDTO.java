package com.erp.model.plm.dto;

import com.erp.model.plm.entity.ProductDetailEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
* @Description 查询多规格明细信息实体类（VO）
* @Author Luo_WG
* @Date 2022/9/22 12:12
**/
@Data
@NoArgsConstructor
public class ProductManyDetailDTO {
    @ApiModelProperty(value = "产品信息列表id")
    private String id;

    @ApiModelProperty(value = "产品图片")
    private String imagesUrl;

    @ApiModelProperty(value = "spu")
    private String spu;

    @ApiModelProperty(value = "产品名称")
    private String name;

    @ApiModelProperty(value = "产品经理")
    private String chargeName;

    @ApiModelProperty(value = "品牌")
    private String brandName;

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

    @ApiModelProperty(value = "产品多规格详情sku信息")
    private List<ProductDetailDTO> productManySkuDetailList;
}
