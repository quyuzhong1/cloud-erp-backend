package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Shopee转运仓列表响应。
 */
@Data
public class TransitWarehouseListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "transit_warehouse_list")
    private List<TransitWarehouse> transitWarehouseList;
}
