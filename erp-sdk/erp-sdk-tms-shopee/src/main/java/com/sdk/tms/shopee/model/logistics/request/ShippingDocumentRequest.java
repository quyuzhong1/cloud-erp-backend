package com.sdk.tms.shopee.model.logistics.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName ShippingDocumentRequest

 * @date 2024年10月15日
 * @version: 1.0
 */
@Data
@Builder
public class ShippingDocumentRequest implements Serializable {
    @NotNull(message = "客户ID不能为空")
    private Long partnerId;
    private Long timestamp;
    @NotBlank(message = "授权token不能为空")
    private String accessToken;
    @NotNull(message = "店铺ID不能为空")
    private Long shopId;
    private String sign;
    private String path;
    private String host;
    @NotBlank(message = "客户key不能为空")
    private String partnerKey;

    /**
     * The list of order you want to create shipping document. limit [1, 50]
     */
    @JSONField(name = "order_list")
    private List<ShippingOrderRequest> orderList;
}
