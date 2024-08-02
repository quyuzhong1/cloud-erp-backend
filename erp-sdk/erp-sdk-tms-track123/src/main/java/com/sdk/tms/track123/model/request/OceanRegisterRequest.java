package com.sdk.tms.track123.model.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
@Builder
public class OceanRegisterRequest implements Serializable {
    /**
     * 物流单号
     */
    @NotBlank(message = "跟踪单号不能为空")
    private String trackingNo;

    /**
     * 单号类型（1.订舱号 2.提单号 3.箱号）
     */
    @NotNull(message = "单号类型不能为空")
    private Integer type;

    /**
     * 承运人对应的唯一简码
     */
    @NotBlank(message = "承运人对应的唯一简码")
    private String carrierCode;

    /**
     * 	客户邮箱,订单关联的客户邮箱
     */
    private String customerEmail;
}
