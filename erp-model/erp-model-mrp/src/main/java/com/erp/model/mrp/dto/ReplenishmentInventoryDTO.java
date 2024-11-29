package com.erp.model.mrp.dto;

import cn.hutool.json.JSONArray;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
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

    /**
     * 预计采购
     */
    private List<EstimatedPurchaseDTO> estimatedPurchaseList;

    /**
     * 补货计划，采购建议
     */
    private List<ReplenishmentPurchaseDTO> replenishmentPurchaseList;

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

    @Getter
    @Setter
    public static class EstimatedPurchaseDTO {
        /**
         * 状态
         */
        private String status;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 预计入库日期
         */
        private LocalDate estimatedPutAwayDate;
        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;
        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 采购订单生成状态
         */
        private String createPoType;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * skuId
         */
        private String skuId;
    }


    @Getter
    @Setter
    public static class ReplenishmentPurchaseDTO {
        /**
         * 状态
         */
        private String status;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 预计入库日期
         */
        private LocalDate estimatedPutAwayDate;
        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;
        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 店铺id
         */
        private String shopId;
    }

    @Getter
    @Setter
    public static class ReplenishmentPurchaseMergeDTO {
        /**
         * 状态
         */
        private String status;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 预计入库日期
         */
        private LocalDate estimatedPutAwayDate;
        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;
        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 明细id
         */
        private JSONArray sourceIdJson;
    }
}
