package com.sdk.tms.shopee.model.firstmile.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 快递寄送信息。
 */
@Data
@Builder
public class CourierDeliveryInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "地址ID不能为空")
    @JSONField(name = "address_id")
    private Long addressId;

    @NotBlank(message = "转运仓ID不能为空")
    @JSONField(name = "warehouse_id")
    private String warehouseId;

    @NotNull(message = "物流产品ID不能为空")
    @JSONField(name = "logistics_product_id")
    private Long logisticsProductId;

    @JSONField(name = "prepaid_account_id")
    private Long prepaidAccountId;

    @NotBlank(message = "快递服务ID不能为空")
    @JSONField(name = "courier_service_id")
    private String courierServiceId;
}
