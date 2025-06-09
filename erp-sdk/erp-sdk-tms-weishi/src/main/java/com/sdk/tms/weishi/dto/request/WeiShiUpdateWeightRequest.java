package com.sdk.tms.weishi.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author liuruipeng
 */
@Data
@Builder
public class WeiShiUpdateWeightRequest {

    //填写reference_no或者order_code或者shipping_method_no,参考
    @NotNull(message = "单号不能为空")
    @JSONField(name = "order_code")
    private String orderCode;

    //kg
    @NotNull(message = "重量不能为空")
    @JSONField(name = "weight")
    private BigDecimal weight;

}
