package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 头程物流渠道。
 */
@Data
public class FirstMileChannel implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "shipment_method")
    private String shipmentMethod;

    @JSONField(name = "logistics_channel_id")
    private Integer logisticsChannelId;

    @JSONField(name = "logistics_channel_name")
    private String logisticsChannelName;
}
