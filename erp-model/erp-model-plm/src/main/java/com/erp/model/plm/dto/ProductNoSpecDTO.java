package com.erp.model.plm.dto;

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

    /**
     * 产品基础信息
     */
    @NotNull(message = "产品基础信息不能为空")
    private ProductBaseInfoDTO productBaseInfoDTO;

    /**
     * 成本信息
     */
    @NotNull(message = "成本信息不能为空")
    private ProductCostDTO productCostDTO;

    /**
     * 采购信息信息
     */
    private ProductPurchaseDTO productPurchaseDTO;

    /**
     * 采购备注信息
     */
    private List<ProductPurchaseRemarkDTO> productPurchaseRemarkList;

    /**
     * 产品销售信息
     */
    @NotNull(message = "产品销售不能为空")
    private ProductSaleDTO productSaleDTO;

    /**
     * 产品物流信息
     */
    private ProductLogisticsDTO productLogisticsDTO;

    /**
     * 产品包装信息
     */
    private ProductPackDTO productPackDTO;

    /**
     * 产品证书信息
     */
    private List<ProductCertificateDTO> productCertificateList;
}
