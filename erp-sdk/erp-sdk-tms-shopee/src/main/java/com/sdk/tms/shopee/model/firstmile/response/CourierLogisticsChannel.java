package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 快递寄送物流产品。
 */
@Data
public class CourierLogisticsChannel implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "logistics_product_id")
    private Long logisticsProductId;

    @JSONField(name = "logistics_product_name")
    private String logisticsProductName;

    @JSONField(name = "courier_list")
    private List<CourierService> courierList;
}
