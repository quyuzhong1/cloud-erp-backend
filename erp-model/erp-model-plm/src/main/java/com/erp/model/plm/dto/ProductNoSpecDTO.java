package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
* @Description: 新增产品无规格sku信息请求参数
* @Author: Luo_WG
* @Date: 2022/9/21 15:46
**/
@Data
@NoArgsConstructor
public class ProductNoSpecDTO {
    @ApiModelProperty(value = "产品sku表id 无id：新增 有id：修改")
    private String id;

    @ApiModelProperty(value = "产品信息表id",required = true)
    private String productId;

    @ApiModelProperty(value = "sku",required = true)
    private String sku;

    @ApiModelProperty(value = "产品名称(品名)",required = true)
    private String name;

    @ApiModelProperty(value = "销售方式 多个用,拼接",required = true)
    private String saleMethod;

    @ApiModelProperty(value = "产品分类id")
    private String categoryId;

    @ApiModelProperty(value = "产品卖点")
    private String productSellSpot;

    @ApiModelProperty(value = "产品功能描述")
    private String productFunctionDesc;

    @ApiModelProperty(value = "产品用途")
    private String usageDesc;

    @StateEnumValue(intValues = {1, 2}, message = "是否存在侵权风险1或者2")
    @ApiModelProperty(value = "存在侵权风险 1：有侵权风险 2：无侵权风险")
    private String pirateRisk;

    @ApiModelProperty(value = "主要材质")
    private String materials;

    @ApiModelProperty(value = "计划上市时间")
    private String planListedTime;

    @ApiModelProperty(value = "单位表id")
    private String unitId;

    @StateEnumValue(intValues = {1, 2, 3, 4, 5}, message = "产品状态错误")
    @ApiModelProperty(value = "产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发",required = true)
    private Integer productState;

    @NotNull(message = "产品基础信息不能为空")
    @ApiModelProperty(value = "产品基础信息",required = true)
    private ProductInfoDTO productInfoDTO;

    @NotNull(message = "成本信息不能为空")
    @ApiModelProperty(value = "成本信息",required = true)
    private ProductCostDTO productCostDTO;

    @ApiModelProperty(value = "采购信息信息")
    private ProductPurchaseDTO productPurchaseDTO;

    @NotNull(message = "产品销售不能为空")
    @ApiModelProperty(value = "产品销售信息")
    private ProductSaleDTO productSaleDTO;

    @ApiModelProperty(value = "产品物流信息")
    private ProductLogisticsDTO productLogisticsDTO;

    @ApiModelProperty(value = "产品包装信息")
    private ProductPackDTO productPackDTO;

    @ApiModelProperty(value = "产品证书信息")
    private ProductCertificateDTO productCertificateDTO;
}
