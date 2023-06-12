package com.erp.model.plm.dto;

import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import com.erp.model.plm.entity.ProductVariantOptionEntity;
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
     * 产品spu基础信息表
     */
    private ProductManySpecBaseDTO productManySpecBaseDTO;

    /**
     * 产品多规格详情sku信息
     */
    private List<ProductDetailEntity> productManySkuDetailList;

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

    /**
     * 产品选中的变体信息
     */
    private List<ProductVariantOptionEntity> productVariantOptionEntityList;


    /**
     * 产品包装辅料
     */
    private List<ProductAccessoriesDTO> productAccessoriesList;

    /**
     * 产品认证信息
     */
    private List<ProductAttestationDTO> productAttestationList;

    /**
     * 目的国海关编码信息
     */
    private List<ProductCustomsEntity> productCustomsList;

}
