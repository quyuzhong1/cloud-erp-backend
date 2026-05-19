package com.sdk.tms.shopee.model.merchant.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Shopee卖家月结账号列表请求。
 */
@Data
@Builder
public class MerchantPrepaidAccountRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "请求地址不能为空")
    private String host;

    private String path;

    private Long timestamp;

    private String sign;

    @NotBlank(message = "授权token不能为空")
    private String accessToken;

    @NotNull(message = "商户ID不能为空")
    private Long merchantId;

    @NotNull(message = "客户ID不能为空")
    private Long partnerId;

    @NotBlank(message = "客户key不能为空")
    private String partnerKey;

    @NotNull(message = "页码不能为空")
    private Integer pageNo;

    @NotNull(message = "每页条数不能为空")
    private Integer pageSize;
}
