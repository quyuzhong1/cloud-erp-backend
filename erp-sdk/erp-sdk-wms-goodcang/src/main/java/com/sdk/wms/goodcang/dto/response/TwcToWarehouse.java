package com.sdk.wms.goodcang.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName TwcToWarehouse
 * @description: TODO
 * @date 2024年11月15日
 * @version: 1.0
 */
@Data
public class TwcToWarehouse implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    //中转仓代码
    @JSONField(name = "transit_warehouse_code")
    private String transitWarehouseCode;

    //中转仓名称
    @JSONField(name = "transit_warehouse_name")
    private String transitWarehouseName;

    //目的仓代码
    @JSONField(name = "warehouse_code")
    private String warehouseCode;

    //目的仓名称
    @JSONField(name = "warehouse_name")
    private String warehouseName;

}
