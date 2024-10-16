package com.erp.model.wms.dto.inventory;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 采购变更单
 * @CreateTime: 2023-05-25  16:16
 * @Author: zhangchunlin
 */
public class InstockForcastPoChangeDetailDTO implements Serializable {

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * sku
         */
        @NotEmpty(message = "sku不能为空")
        private String skuId;

        /**
         * sku
         */
        @NotEmpty(message = "sku不能为空")
        private String skuNo;

        /**
         * 采购订单明细id
         */
        @NotEmpty(message = "采购订单明细id不能为空")
        private String purchaseOrderDetailId;
        /**
         * 采购变更单明细id
         */
        private String purchaseOrderChangeDetailId;

        /**
         * 执行状态
         */
        private String executionStatus;

        /**
         * 原采购订单数量
         */
        @NotNull(message = "数量不能为空")
        @Max(value = 999999999, message = "数量最大值为999999999")
        private Integer originQty;

        /**
         * 变更后的采购订单数量
         */
        @NotNull(message = "数量不能为空")
        @Max(value = 999999999, message = "数量最大值为999999999")
        private Integer qty;
    }

}