package com.sdk.wms.goodcang.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author liuruipeng
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GoodCangGetOutBoundReq extends GoodCangBaseRequest {

    //订单号
    @JSONField(name = "order_code")
    private String orderCode;

    //订单号
    @JSONField(name = "order_code_arr")
    private List<String> orderCodeArr;

    //物流产品代码
    @JSONField(name = "shipping_method")
    private String shippingMethod;

    //订单创建开始时间
    @JSONField(name = "create_date_from")
    private LocalDateTime createDateFrom;

    //订单创建结束时间
    @JSONField(name = "create_date_to")
    private LocalDateTime createDateTo;

    //订单修改开始时间
    @JSONField(name = "modify_date_from")
    private LocalDateTime modifyDateFrom;

    //订单修改结束时间
    @JSONField(name = "modify_date_to")
    private LocalDateTime modifyDateTo;

    //订单物流开始时间
    @JSONField(name = "date_shipping_from")
    private LocalDateTime dateShippingFrom;

    //订单物流结束时间
    @JSONField(name = "date_shipping_to")
    private LocalDateTime dateShippingTo;

    //订单状态
    @JSONField(name = "order_status")
    private String orderStatus;

    //提货状态
    @JSONField(name = "ship_status")
    private Integer shipStatus;

}
