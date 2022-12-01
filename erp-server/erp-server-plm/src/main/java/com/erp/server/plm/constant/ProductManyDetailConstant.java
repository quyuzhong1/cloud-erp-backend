package com.erp.server.plm.constant;

import org.apache.ibatis.annotations.Many;
import org.apache.poi.ss.formula.functions.T;

/**
 * @Classname ProductManyDetailConstant
 * @Description TODO
 * @Date 2022-11-28 11:11
 * @Created by yl
 */
public interface ProductManyDetailConstant {

    /**
     * 产品spu基础信息
     */
    String  PRODUCT_MANY_SPEC_BASE ="productManySpecBaseDTO";

    /**
     * 产品多规格详情sku信息
     */
    String  PRODUCT_MANY_SKU_DETAIL_LIST ="productManySkuDetailList";


    /**
     * 产品成本信息
     */
    String  PRODUCT_COST_SHOW_LIST ="productCostShowDTOList";


    /**
     * 产品采购信息
     */
    String  PRODUCT_PURCHASE_SHOW_LIST ="productPurchaseShowDTOList";


    /**
     * 备注信息
     */
    String  REMARK_ENTITY_LIST ="remarkEntityList";


    /**
     * 产品销售信息
     */
    String  PRODUCT_SALE_SHOW_LIST ="productSaleShowDTOList";


    /**
     * 产品包装信息
     */
    String  PRODUCT_PACK_SHOW ="productPackShowDTOS";

    /**
     * 产品物流信息
     */
    String PRODUCT_LOGISTICS_SHOW_LIST ="productLogisticsShowDTOList";


    /**
     * 产品证书信息
     */
    String  PRODUCT_CERTIFICATE_SHOW_LIST ="productCertificateShowDTOList";


    /**
     * 产品选中的变体信息
     */
    String  PRODUCT_VARIANT_OPTION_ENTITY_LIST ="productVariantOptionEntityList";



}
