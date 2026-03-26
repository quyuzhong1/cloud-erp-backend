package com.common.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * FBT货件DTO，DMP/WMS之间的统一消息体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PlatformFbtShipmentDTO extends UniqueDto {

    /**
     * 授权ID
     */
    private String authId;

    /**
     * 店铺ID
     */
    private String shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 入库单号
     */
    private String inboundOrderId;

    /**
     * 货件名称
     */
    private String shipmentName;

    /**
     * 平台仓库编码
     */
    private String warehouseCode;

    /**
     * 平台仓库名称
     */
    private String warehouseName;

    /**
     * 平台货件状态
     */
    private String platformShipmentStatus;

    /**
     * 平台更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 承运商列表
     */
    private List<CarrierDTO> carrierList = new ArrayList<>();

    /**
     * 计划明细
     */
    private List<PlannedGoodDTO> plannedGoods = new ArrayList<>();

    /**
     * 签收批次
     */
    private List<ReceivedBatchDTO> receivedBatches = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class CarrierDTO {
        private String carrierName;
        private String trackingNumber;
    }

    @Data
    @NoArgsConstructor
    public static class PlannedGoodDTO {
        private String goodsId;
        private String referenceCode;
        private String name;
        private Integer quantity;
        private List<String> skuIds = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class ReceivedBatchDTO {
        private String batchId;
        private String goodsId;
        private Integer normalQuantity;
        private Integer defectiveQuantity;
        private Integer totalQuantity;
        private List<String> productIds = new ArrayList<>();
        private List<String> skuIds = new ArrayList<>();
    }
}
