package com.erp.model.plm.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
* @Description: 新增产品多规格sku信息请求参数
* @Author: Luo_WG
* @Date: 2022/9/21 15:46
**/
@Data
@NoArgsConstructor
public class ProductManySpecDTO {

    @ApiModelProperty(value = "产品信息表id",required = true)
    private String productId;

    @ApiModelProperty(value = "spu",required = true)
    private String spu;

    @ApiModelProperty(value = "产品名称(品名)",required = true)
    private String name;

    @ApiModelProperty(value = "销售方式",required = true)
    private List<String> saleMethod;

    @ApiModelProperty(value = "产品分类id")
    private String categoryId;

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

    @ApiModelProperty(value = "SKU信息明细")
    private List<ProductDetailDTO>  productDetailList;
}
