package com.erp.model.wms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TiktokFbtDTO {

    @Data
    @NoArgsConstructor
    public static class WebhookReqDTO {
        @JsonProperty("type")
        private Integer eventType;
        @JsonProperty("seller_open_id")
        private String sellerOpenId;
        @JsonProperty("tts_notification_id")
        private String ttsNotificationId;
        private Long timestamp;
        private DataDTO data;

        @Data
        @NoArgsConstructor
        public static class DataDTO {
            @JsonProperty("inbound_order_id")
            private String inboundOrderId;
            @JsonProperty("order_status")
            private String orderStatus;
            @JsonProperty("update_time")
            private Long updateTime;
        }
    }

    @Data
    @NoArgsConstructor
    public static class SyncReqDTO {
        @NotBlank(message = "inboundOrderId不能为空")
        private String inboundOrderId;
    }

    @Data
    @NoArgsConstructor
    public static class InboundOrderDTO {
        private String shopId;
        private String inboundOrderId;
        private String shipmentName;
        private String warehouseCode;
        private String status;
        private LocalDateTime updatedTime;
        private List<String> goodsIds = new ArrayList<>();
        private List<String> fbtWarehouseIds = new ArrayList<>();
        private List<PlannedGoodDTO> plannedGoods = new ArrayList<>();
        private List<ReceivedBatchDTO> receivedBatches = new ArrayList<>();
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

    @Data
    @NoArgsConstructor
    public static class InventoryRecordDTO {
        private String recordId;
        private String shopId;
        private String inboundOrderId;
        private String warehouseCode;
        private String skuCode;
        private String goodsId;
        private Integer deltaQty;
        private LocalDateTime eventTime;
    }

    @Data
    @NoArgsConstructor
    public static class PageResultDTO<T> {
        private List<T> records = new ArrayList<>();
        private String nextPageToken;
        private Boolean hasMore = Boolean.FALSE;
    }
}
