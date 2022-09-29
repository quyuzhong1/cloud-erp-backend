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

    @NotNull(message = "产品基础信息不能为空")
    @ApiModelProperty(value = "产品基础信息",required = true)
    private ProductBaseInfoDTO productBaseInfoDTO;

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
