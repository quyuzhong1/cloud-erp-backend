package com.erp.model.plm.dto;

import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname ProductDetaildto
 * @Description TODO
 * @Date 2023-01-11 15:55
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BomSkuDetailDTO implements Serializable {


    /**
     * 产品成本信息
     */
    private List<ProductCostShowDTO> productCostShowDTOList;


    /**
     * 产品采购信息
     */
    private List<ProductPurchaseShowDTO> productPurchaseShowDTOList;


    /**
     * 产品采购备注信息
     */
    private List<ProductPurchaseRemarkEntity> remarkEntityList;


    /**
     * 产品销售信息
     */
    private List<ProductSaleShowDTO> productSaleShowDTOList;


    /**
     * 产品包装信息
     */
    private List<ProductPackShowDTO> productPackShowDTOS;


    /**
     * 产品物流信息
     */
    private List<ProductLogisticsShowDTO> productLogisticsShowDTOList;


    /**
     * 产品证书信息
     */
    private List<ProductCertificateShowDTO> productCertificateShowDTOList;
}
