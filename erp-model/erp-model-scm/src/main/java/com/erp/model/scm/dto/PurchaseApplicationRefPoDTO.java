package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/23 14:31
 */
@Data
@NoArgsConstructor
public class PurchaseApplicationRefPoDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 采购申请明细id
         */
        private String purchaseApplicationDetailId;

        /**
         * 采购申请id
         */
        private String purchaseApplicationId;

        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 已采购数量
         */
        private Integer purchaseQty;
    }
}
