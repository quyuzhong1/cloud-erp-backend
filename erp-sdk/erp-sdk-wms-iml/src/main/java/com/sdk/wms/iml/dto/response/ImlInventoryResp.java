package com.sdk.wms.iml.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class ImlInventoryResp implements Serializable {

    //SKU
    @JSONField(name = "product_sku")
    private String productSku;

    //仓库代码
    @JSONField(name = "warehouse_code")
    private String warehouseCode;

    //在途数量
    @JSONField(name = "onway")
    private Integer onway;

    //待上架数量
    @JSONField(name = "pending")
    private Integer pending;

    //可售数量
    @JSONField(name = "sellable")
    private Integer sellable;

    //不合格数量
    @JSONField(name = "unsellable")
    private Integer unsellable;

    //待出库数量
    @JSONField(name = "reserved")
    private Integer reserved;

    //历史出库数量
    @JSONField(name = "shipped")
    private Integer shipped;

    //待确认数量
    @JSONField(name = "unconfirmed")
    private Integer unconfirmed;
}
