package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReplenishmentInventoryDTO {
    /**
     * fba可用
     */
    private List<FbaUsableDTO> fbaUsableList;
    /**
     * 海外仓可用
     */
    private List<OverseasUsableDTO> overseasUsableList;

    /**
     * 本地仓可用
     */
    private List<LocalUsableDTO> localUsableList;

    /**
     * 虚拟仓可用
     */
    private List<VirtualUsableDTO> virtualUsableList;

    @Setter
    @Getter
    public static class FbaUsableDTO {
        /**
         * sku
         */
        private String skuNo;
        /**
         * 仓库
         */
        private String warehouseId;
        /**
         * 数量
         */
        private Integer qty;
    }


    @Setter
    @Getter
    public static class OverseasUsableDTO {
        /**
         * sku
         */
        private String skuId;
        /**
         * 仓库
         */
        private String warehouseCode;
        /**
         * 数量
         */
        private Integer qty;
    }

    @Getter
    @Setter
    public static class LocalUsableDTO {
        /**
         * sku
         */
        private String skuId;
        /**
         * 仓库
         */
        private String warehouseId;
        /**
         * 数量
         */
        private Integer qty;
    }

    @Getter
    @Setter
    public static class VirtualUsableDTO {
        /**
         * sku
         */
        private String skuId;
        /**
         * 仓库
         */
        private String virtualWarehouseId;
        /**
         * 数量
         */
        private Integer qty;
    }
}
