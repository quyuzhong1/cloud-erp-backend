package com.erp.model.plm.dto;

import com.erp.model.plm.entity.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
* @Description 查询无规格所有明细信息实体类（VO）
* @Author Luo_WG
* @Date 2022/9/22 14:46
**/
@Data
@NoArgsConstructor
public class ProductManySpecDetailAllDTO {
    /**
     * 产品基础信息
     */
    @NotNull(message = "产品基础信息不能为空")
    @ApiModelProperty(value = "产品基础信息", required = true)
    private ProductInfoEntity productInfoEntity;

    /**
     * SKU信息明细
     */
    @ApiModelProperty(value = "SKU信息明细")
    private List<ProductDetailEntity> productDetailList;

    /**
     * 成本信息
     */
    @NotNull(message = "成本信息不能为空")
    @ApiModelProperty(value = "成本信息", required = true)
    private List<ProductCostEntity> productCostList;

    /**
     * 采购信息信息
     */
    @ApiModelProperty(value = "采购信息信息")
    private List<ProductPurchaseEntity> productPurchaseList;

    /**
     * 产品销售信息
     */
    @NotNull(message = "产品销售不能为空")
    @ApiModelProperty(value = "产品销售信息")
    private List<ProductSaleEntity> productSaleList;

    /**
     * 产品物流信息
     */
    @ApiModelProperty(value = "产品物流信息")
    private List<ProductLogisticsEntity> productLogisticsList;

    /**
     * 产品包装信息
     */
    @ApiModelProperty(value = "产品包装信息")
    private List<ProductPackEntity> productPackList;

    /**
     * 产品证书信息
     */
    @ApiModelProperty(value = "产品证书信息")
    private List<ProductCertificateEntity> productCertificateList;

    /**
     * 采购备注信息
     */
    @ApiModelProperty(value = "采购备注信息")
    private List<ProductPurchaseRemarkEntity> productPurchaseRemarkList;


}
