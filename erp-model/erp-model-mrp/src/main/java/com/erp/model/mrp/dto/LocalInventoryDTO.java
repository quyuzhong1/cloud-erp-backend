package com.erp.model.mrp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocalInventoryDTO {
    /**
     * 仓库id
     */
    private String warehouseId;
    /**
     * 数量
     */
    private Integer qty;

    /**
     * 来源类型
     */
    private String sourceType;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopSalesDTO {
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 销量
         */
        private Integer qty;
    }


    @Getter
    @Setter
    public static class OverseasInventoryDTO {
        /**
         * 仓库编码
         */
        private String warehouseCode;
        /**
         * 数量
         */
        private Integer qty;
    }
}
