package com.erp.model.mrp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

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
}
