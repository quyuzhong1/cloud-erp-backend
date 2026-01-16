package com.sdk.wms.antu.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.config.FastJson2LocalDateTimeDeserializer;
import com.common.business.dto.CleanBaseDTO;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class AntuOutboundResp extends CleanBaseDTO implements Serializable {

    //订单号
    @JSONField(name = "order_code")
    private String orderCode;
    //公共平台单号
    @JSONField(name = "sw_order_number")
    private String swOrderNumber;

    //客户参考号
    @JSONField(name = "reference_no")
    private String referenceNo;

    //订单状态
    @JSONField(name = "order_status")
    private String orderStatus;

    //出库时间
    @JSONField(name = "date_shipping",deserializeUsing = FastJson2LocalDateTimeDeserializer.class)
    private LocalDateTime outBoundTime;

    //跟踪号
    @JSONField(name = "tracking_no")
    private String trackNo;
    //异常信息
    @JSONField(name = "abnormal_reason")
    private String abnormalReason;
    //仓库代码
    @JSONField(name = "warehouse_code")
    private String warehouseCode;
    //运输方式
    @JSONField(name = "shipping_method")
    private String shippingMethod;
    //承运商
    @JSONField(name = "carrier_name")
    private String carrierName;

}
