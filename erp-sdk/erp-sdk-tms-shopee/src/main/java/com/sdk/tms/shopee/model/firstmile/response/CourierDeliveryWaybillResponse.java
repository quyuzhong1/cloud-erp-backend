package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 快递寄送模式获取面单响应。
 */
@Data
public class CourierDeliveryWaybillResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "waybill_list")
    private List<CourierDeliveryWaybill> waybillList;
}
