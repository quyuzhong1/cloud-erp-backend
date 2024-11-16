package com.sdk.wms.goodcang.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.dto.CleanBaseDTO;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class GoodCangReceiptBatchResp extends CleanBaseDTO implements Serializable {

    //入库单号
    @JSONField(name = "receiving_code")
    private String receivingCode;

    //入库单状态
    @JSONField(name = "receiving_status")
    private Integer receivingStatus;

    //入库单类型
    @JSONField(name = "transit_type")
    private Integer transitType;

    //尾程仓收货批次
    @JSONField(name = "overseas_detail")
    private List<GcReceiving> gcReceivingDataList;

    @EqualsAndHashCode(callSuper = true)
    @Data
    @ToString
    public static class GcReceiving extends CleanBaseDTO {

        //入库单箱号
        @JSONField(name = "box_no")
        private String boxNo;

        //商品高度
        @JSONField(name = "product_height")
        private Float productHeight;

        //商品长度
        @JSONField(name = "product_length")
        private Float productLength;

        //商品SKU
        @JSONField(name = "product_sku")
        private String productSku;

        //商品重量
        @JSONField(name = "product_weight")
        private Float productWeight;

        //商品宽度
        @JSONField(name = "product_width")
        private Float productWidth;

        //预报数量
        @JSONField(name = "quantity")
        private Integer quantity;

        //收货数量
        @JSONField(name = "overseas_shelves_count")
        private Integer receivedQty;

        //入库单号
        @JSONField(name = "receiving_code")
        private String receivingCode;
    }
}
