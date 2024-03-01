package com.erp.model.dmp.lingxing;

import com.common.business.dto.CleanBaseDTO;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class FbaReceiveDetailEntity{

    // 店铺id
    @Panno(findType = PannoEnum.EQ,field = "sid")
    private Integer sid;

    // 接收日期
    @Panno(findType = PannoEnum.EQ,field = "receivedDateStr")
    private String receivedDateStr;

    // 当地接收日期
    @Panno(findType = PannoEnum.EQ,field = "receivedDateLocaleStr")
    private String receivedDateLocaleStr;

    // 接收日期时间戳
    @Panno(findType = PannoEnum.EQ,field = "receivedDateTimestamp")
    private Long receivedDateTimestamp;

    // FNSKU
    @Panno(findType = PannoEnum.EQ,field = "fnsku")
    private String fnsku;

    // MSKU
    @Panno(findType = PannoEnum.EQ,field = "sku")
    private String sku;

    // Listing标题
    @Panno(findType = PannoEnum.EQ,field = "productName")
    private String productName;

    // 数量
    @Panno(findType = PannoEnum.EQ,field = "quantity")
    private Integer quantity;

    // fba_shipment_id
    @Panno(findType = PannoEnum.EQ,field = "fbaShipmentId")
    private String fbaShipmentId;

    // fulfillment_center_id
    @Panno(findType = PannoEnum.EQ,field = "fulfillmentCenterId")
    private String fulfillmentCenterId;

    // 单内签收日期索引
    @Panno(findType = PannoEnum.EQ,field = "uniqueIndex")
    private Integer uniqueIndex;

    // 与unique_index组成唯一索引
    @Panno(findType = PannoEnum.EQ,field = "uniqueMd5")
    private String uniqueMd5;

    // 处理过的接收日期
    @Panno(findType = PannoEnum.EQ,field = "receivedDateReport")
    private LocalDate receivedDateReport;

    @Panno(findType = PannoEnum.EQ,field = "newSource")
    private Integer newSource;
}
