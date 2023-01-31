package com.erp.model.plm.dto;

import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 产品信息的最小拆分的信息
 * 对应 变更信息里面的
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
    private ProductCostShowDTO productCostShowDTO;

    /**
     * 产品采购信息
     */
    private ProductPurchaseShowDTO productPurchaseShowDTO;

    /**
     * 产品采购备注信息
     */
    private List<ProductPurchaseRemarkEntity> remarkEntityList;

    /**
     * 产品销售信息
     */
    private ProductSaleShowDTO productSaleShowDTO;

    /**
     * 产品包装信息
     */
    private ProductPackShowDTO productPackShowDTO;

    /**
     * 产品物流信息
     */
    private ProductLogisticsShowDTO productLogisticsShowDTO;

    /**
     * 产品证书信息
     */
    private List<ProductCertificateShowDTO> productCertificateShowDTOList;

}
