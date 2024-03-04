package com.sdk.third.lingxing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * FBA签收明细响应体
 */
@Data
@NoArgsConstructor
public class FbaShipmentReceiveDTO {

    // 店铺id
    @JsonProperty("sid")
    private Integer sid;

    // 接收日期
    @JsonProperty("received_date")
    private OffsetDateTime receivedDate;

    // 当地接收日期
    @JsonProperty("received_date_locale")
    private OffsetDateTime receivedDateLocale;

    // 接收日期时间戳
    @JsonProperty("received_date_timestamp")
    private Long receivedDateTimestamp;

    // FNSKU
    @JsonProperty("fnsku")
    private String fnsku;

    // MSKU
    @JsonProperty("sku")
    private String sku;

    // Listing标题
    @JsonProperty("product_name")
    private String productName;

    // 数量
    @JsonProperty("quantity")
    private Integer quantity;

    // fba_shipment_id
    @JsonProperty("fba_shipment_id")
    private String fbaShipmentId;

    // fulfillment_center_id
    @JsonProperty("fulfillment_center_id")
    private String fulfillmentCenterId;

    // 单内签收日期索引
    @JsonProperty("unique_index")
    private Integer uniqueIndex;

    // 与unique_index组成唯一索引
    @JsonProperty("unique_md5")
    private String uniqueMd5;

    // 处理过的接收日期
    @JsonProperty("received_date_report")
    private LocalDate receivedDateReport;

    @JsonProperty("new_source")
    private Integer newSource;


}
