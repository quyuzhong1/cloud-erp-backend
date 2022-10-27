package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
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
     * 产品基础信息
     */
    @NotNull(message = "产品基础信息不能为空")
    @Valid
    private ProductInfoDTO productInfoDTO;

    /**
     * SKU信息明细
     */
    private List<ProductDetailDTO> productDetailList;

    /**
     * 成本信息
     */
    @NotNull(message = "成本信息不能为空")
    private List<ProductCostDTO> productCostList;

    /**
     * 采购信息信息
     */
    private List<ProductPurchaseDTO> productPurchaseList;

    /**
     * 产品销售信息
     */
    @NotNull(message = "产品销售不能为空")
    private List<ProductSaleDTO> productSaleList;

    /**
     * 产品物流信息
     */
    private List<ProductLogisticsDTO> productLogisticsList;

    /**
     * 产品包装信息
     */
    private List<ProductPackDTO> productPackList;

    /**
     * 产品证书信息
     */
    private List<ProductCertificateDTO> productCertificateList;

    /**
     * 采购备注信息
     */
    private List<ProductPurchaseRemarkDTO> productPurchaseRemarkList;
}
