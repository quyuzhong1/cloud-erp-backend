package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 头程物流渠道列表响应。
 */
@Data
public class FirstMileChannelListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "logistics_channel_list")
    private List<FirstMileChannel> logisticsChannelList;
}
