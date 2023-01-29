package com.erp.model.plm.dto;

import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 产品信息的最小拆分的信息
 *
 * @Classname
 * @Description TODO
 * @Date 2023-01-29 11:12
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductSmallestUnitDTO implements Serializable {


    /**
     *产品spu基础信息表
     */
    private ProductManySpecBaseDTO productManySpecBaseDTO;

    /**
     * 产品多规格详情sku信息
     */
    private ProductDetailEntity productManySkuDetail;


    /**
     * 产品成本信息
     */
    private List<ProductCostShowDTO> productCostShowDTOList;

    /**
     * 产品采购信息
     */
    private List<ProductPurchaseShowDTO> productPurchaseShowDTOList;

    /**
     * 产品采购信息
     */
    private List<ProductPurchaseRemarkEntity> remarkEntityList;

    /**
     * 产品包装信息
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
