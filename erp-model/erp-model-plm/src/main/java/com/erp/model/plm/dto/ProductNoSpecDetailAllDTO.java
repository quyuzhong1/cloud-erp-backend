package com.erp.model.plm.dto;

import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
* @Description 查询无规格所有明细信息实体类（VO）
* @Author Luo_WG
* @Date 2022/9/22 14:46
**/
@Data
@NoArgsConstructor
public class ProductNoSpecDetailAllDTO {
    /**
     * 产品基础信息
     */
    @ApiModelProperty(value = "产品基础信息")
    private ProductNoDetailDTO productNoDetailDTO;


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
