package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SKU采购信息
 * @CreateTime: 2023-06-19  18:17
 * @Author: zhangchunlin
 */
public class SkuPurchaseDTO implements Serializable {

    /**
     * SKU采购员、供应商信息
     */
    @Data
    @NoArgsConstructor
    public static class PurchaseInfo {

        /**
         * sku id
         */
        private String skuId;

        /**
         * 采购员id
         */
        private String purchaseUserId;

        /**
         * 采购员名称
         */
        private String purchaseUserName;

        /**
         * 一级供应商id
         */
        private String supplierId;

        /**
         * 一级供应商名称
         */
        private String supplierName;

    }

}