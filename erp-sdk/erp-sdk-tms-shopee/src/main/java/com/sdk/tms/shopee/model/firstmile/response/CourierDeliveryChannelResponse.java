package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 虾皮快递寄送渠道列表响应。
 */
@Data
public class CourierDeliveryChannelResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "logistics_channel_list")
    private List<CourierLogisticsChannel> logisticsChannelList;
}
