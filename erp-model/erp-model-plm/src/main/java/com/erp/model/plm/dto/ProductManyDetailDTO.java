package com.erp.model.plm.dto;

import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
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

    /**
     * 产品信息列表id
     */
    @ApiModelProperty(value = "产品信息列表id")
    private String id;

    /**
     * 产品图片
     */
    @ApiModelProperty(value = "产品图片")
    private String imagesUrl;

    /**
     * spuNo
     */
    @ApiModelProperty(value = "spuNo")
    private String spuNo;

    /**
     * 产品名称
     */
    @ApiModelProperty(value = "产品名称")
    private String name;

    /**
     * 产品经理
     */
    @ApiModelProperty(value = "产品经理")
    private String chargeName;

    /**
     * 品牌
     */
    @ApiModelProperty(value = "品牌")
    private String brandName;

    /**
     * 产品属性
     */
    @ApiModelProperty(value = "产品属性")
    private String property;

    /**
     * 产品等级
     */
    @ApiModelProperty(value = "产品等级")
    private String grade;

    /**
     * 产品类别
     */
    @ApiModelProperty(value = "产品类别")
    private String category;

    /**
     * 销售方式
     */
    @ApiModelProperty(value = "销售方式")
    private String saleMethod;

    /**
     * 产品卖点
     */
    @ApiModelProperty(value = "产品卖点")
    private String productSellSpot;

    /**
     * 产品功能描述
     */
    @ApiModelProperty(value = "产品功能描述")
    private String functionDesc;

    /**
     * 产品用途
     */
    @ApiModelProperty(value = "产品用途")
    private String usageDesc;

    /**
     * 存在侵权风险 1：有侵权风险 2：无侵权风险
     */
    @ApiModelProperty(value = "存在侵权风险 1：有侵权风险 2：无侵权风险")
    private String pirateRisk;

    /**
     * 主要材质
     */
    @ApiModelProperty(value = "主要材质")
    private String materials;

    /**
     * 产品多规格详情sku信息
     */
    @ApiModelProperty(value = "产品多规格详情sku信息")
    private List<ProductDetailEntity> productManySkuDetailList;

    /**
     * 产品成本信息
     */
    @ApiModelProperty(value = "产品成本信息")
    private List<ProductCostShowDTO> productCostShowDTOList;

    /**
     * 产品采购信息
     */
    @ApiModelProperty(value = "产品采购信息")
    private List<ProductPurchaseShowDTO> productPurchaseShowDTOList;

    /**
     * 产品采购信息
     */
    @ApiModelProperty(value = "产品采购信息")
    private List<ProductPurchaseRemarkEntity> remarkEntityList;

    /**
     * 产品包装信息
     */
    private List<ProductSaleShowDTO> productSaleShowDTOList;

    /**
     * 产品包装信息
     */
    @ApiModelProperty(value = "产品包装信息")
    private List<ProductPackShowDTO> productPackShowDTOS;

    /**
     * 产品物流信息
     */
    @ApiModelProperty(value = "产品物流信息")
    private List<ProductLogisticsShowDTO> productLogisticsShowDTOList;

    /**
     * 产品证书信息
     */
    @ApiModelProperty(value = "产品证书信息")
    private List<ProductCertificateShowDTO> productCertificateShowDTOList;
}
