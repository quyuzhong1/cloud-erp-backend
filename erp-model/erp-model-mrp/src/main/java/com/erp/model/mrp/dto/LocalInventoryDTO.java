package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalInventoryDTO {
    /**
     * 仓库id
     */
    private String warehouseId;
    /**
     * 数量
     */
    private Integer qty;


    @Getter
    @Setter
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
}
