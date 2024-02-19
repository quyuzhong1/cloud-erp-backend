package com.erp.model.dmp.lingxing;

import com.common.business.dto.CleanBaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class FbaReceiveDetailEntity extends CleanBaseDTO {

    // 店铺id
    private Integer sid;

    // 接收日期
    private OffsetDateTime receivedDate;

    // 当地接收日期
    private OffsetDateTime receivedDateLocale;

    // 接收日期时间戳
    private Long receivedDateTimestamp;

    // FNSKU
    private String fnsku;

    // MSKU
    private String sku;

    // Listing标题
    private String productName;

    // 数量
    private Integer quantity;

    // fba_shipment_id
    private String fbaShipmentId;

    // fulfillment_center_id
    private String fulfillmentCenterId;

    // 单内签收日期索引
    private Integer uniqueIndex;

    // 与unique_index组成唯一索引
    private String uniqueMd5;

    // 处理过的接收日期
    private LocalDate receivedDateReport;

    private Integer newSource;
}
