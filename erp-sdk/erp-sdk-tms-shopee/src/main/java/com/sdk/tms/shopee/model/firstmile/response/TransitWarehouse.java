package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * Shopee转运仓信息。
 */
@Data
public class TransitWarehouse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "warehouse_id")
    private String warehouseId;

    @JSONField(name = "warehouse_name_en")
    private String warehouseNameEn;

    @JSONField(name = "warehouse_name_cn")
    private String warehouseNameCn;

    @JSONField(name = "warehouse_type")
    private Integer warehouseType;
}
