package com.erp.model.plm.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description: 新增产品多规格sku信息请求参数
 * @Author: Luo_WG
 * @Date: 2022/9/21 15:46
 **/
@Data
@NoArgsConstructor
public class ProductManySpecDTO {

    /**
     * 产品信息表id
     */
    @ApiModelProperty(value = "产品信息表id", required = true)
    private String productId;

    /**
     * spuNo
     */
    @ApiModelProperty(value = "spuNo", required = true)
    private String spuNo;

    /**
     * 产品名称(品名)
     */
    @ApiModelProperty(value = "产品名称(品名)", required = true)
    private String name;

    /**
     * 销售方式
     */
    @ApiModelProperty(value = "销售方式", required = true)
    private String saleMethod;

    /**
     * 产品分类id
     */
    @ApiModelProperty(value = "产品分类id")
    private String categoryId;

    /**
     * 产品卖点
     */
    @ApiModelProperty(value = "产品卖点")
    private String productSellSpot;

    /**
     * 产品功能描述
     */
    @ApiModelProperty(value = "产品功能描述")
    private String productFunctionDesc;

    /**
     * 产品用途
     */
    @ApiModelProperty(value = "产品用途")
    private String usageDesc;

    /**
     * 存在侵权风险
     */
    @ApiModelProperty(value = "存在侵权风险 1：有侵权风险 2：无侵权风险")
    private String pirateRisk;

    /**
     * 主要材质
     */
    @ApiModelProperty(value = "主要材质")
    private String materials;

    /**
     * SKU信息明细
     */
    @ApiModelProperty(value = "SKU信息明细")
    private List<ProductDetailDTO> productDetailList;

    /**
     * 产品基础信息
     */
    @NotNull(message = "产品基础信息不能为空")
    @ApiModelProperty(value = "产品基础信息", required = true)
    private ProductInfoDTO productInfoDTO;

    /**
     * 成本信息
     */
    @NotNull(message = "成本信息不能为空")
    @ApiModelProperty(value = "成本信息", required = true)
    private List<ProductCostDTO> productCostList;

    /**
     * 采购信息信息
     */
    @ApiModelProperty(value = "采购信息信息")
    private List<ProductPurchaseDTO> productPurchaseList;

    /**
     * 产品销售信息
     */
    @NotNull(message = "产品销售不能为空")
    @ApiModelProperty(value = "产品销售信息")
    private List<ProductSaleDTO> productSaleList;

    /**
     * 产品物流信息
     */
    @ApiModelProperty(value = "产品物流信息")
    private List<ProductLogisticsDTO> productLogisticsList;

    /**
     * 产品包装信息
     */
    @ApiModelProperty(value = "产品包装信息")
    private List<ProductPackDTO> productPackList;

    /**
     * 产品证书信息
     */
    @ApiModelProperty(value = "产品证书信息")
    private List<ProductCertificateDTO> productCertificateList;
}
