package com.sdk.wms.antu.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.config.FastJson2LocalDateTimeDeserializer;
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
public class AntuReturnResp extends CleanBaseDTO implements Serializable {

    //退件单号
    @JSONField(name = "spo_code")
    private String returnCode;

    //状态 0:已作废 1:待确认 2:在途 3:到货 4:到货异常 5:已完成
    @JSONField(name = "spo_status")
    private String status;

    //平台单号
    @JSONField(name = "refrence_no_platform")
    private String orderCode;

    //退件类型 1:买家退件 2:物流退件 3:认领
    @JSONField(name = "spo_type")
    private String spoType;


    //订单参考号
    @JSONField(name = "order_reference_no")
    private String orderReferenceNo;

    //完成时间
    @JSONField(name = "spo_complete_time",deserializeUsing = FastJson2LocalDateTimeDeserializer.class)
    private LocalDateTime completeTime;

    //创建时间
    @JSONField(name = "spo_add_time",deserializeUsing = FastJson2LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;

    //仓库代码
    @JSONField(name = "warehouse_code")
    private String warehouseCode;

    //退货原因
    @JSONField(name = "spo_desc")
    private String reason;


    @JSONField(name = "detail")
    private List<Detail> details;

    @Data
    @ToString
    public static class Detail implements Serializable{

        //SKU
        @JSONField(name = "product_barcode")
        private String plarformSkuNo;

        //良品数量
        @JSONField(name = "ok_qty")
        private Integer okQty;

        //不良品数量
        @JSONField(name = "sop_unsellable_qty")
        private Integer sopUnsellableQty;

        //上架数量
        @JSONField(name = "putaway_quantity")
        private Integer putawayQuantity;

        //上架数
        @JSONField(name = "putaway_qty")
        private Integer putawayQty;

        //更新时间
        @JSONField(name = "rd_update_time",deserializeUsing = FastJson2LocalDateTimeDeserializer.class)
        private LocalDateTime rdUpdateTime;

        //箱号
        @JSONField(name = "box_no")
        private Integer boxNo;

        //库位类型统计数量,0:良品,1:不良品,2:暂存
        @JSONField(name = "loCountType")
        private String loCountType;

    }
}
