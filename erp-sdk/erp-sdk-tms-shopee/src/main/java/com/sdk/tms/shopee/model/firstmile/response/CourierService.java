package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 快递服务公司信息。
 */
@Data
public class CourierService implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "courier_name")
    private String courierName;

    @JSONField(name = "courier_service_id")
    private String courierServiceId;

    @JSONField(name = "courier_service_name")
    private String courierServiceName;
}
