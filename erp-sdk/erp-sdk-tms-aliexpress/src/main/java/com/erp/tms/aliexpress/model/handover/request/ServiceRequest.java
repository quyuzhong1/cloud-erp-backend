package com.erp.tms.aliexpress.model.handover.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.erp.tms.aliexpress.model.handover.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zdy
 * @ClassName SubbagRequest
 * @description: 批次追加大包 请求
 * @date 2024年02月02日
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest implements Serializable {
    /**
     *交易订单号
     * 是
     */
    @NotBlank(message = "交易订单号不能为空")
    @JSONField(name = "trade_order_id")
    private String tradeOrderId;
    /**
     * 国际运单号
     */
    @NotBlank(message = "国际运单号不能为空")
    @JSONField(name = "intl_tracking_no")
    private String intlTrackingNo;
    /**
     *
     *物流单号（LP）
     */
    @NotBlank(message = "物流单号不能为空")
    @JSONField(name = "out_order_code")
    private String outOrderCode;
    /**
     * 原因
     */
    @NotBlank(message = "原因\n不能为空")
    @JSONField(name = "reason")
    private String reason;
}
