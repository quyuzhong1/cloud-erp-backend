package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

@NoArgsConstructor
public enum SourceTypeEnum {
    SELF_ADD("selfAdd", "手动新增"),
    PURCHASE_ORDER("purchaseOrder", "采购订单"),
    PURCHASE_STOCK_IN("purchaseStockIn", "采购入库单"),
    QC_BILL("qcBill", "质检单"),
    PURCHASE_RETURN_ORDER("purchaseReturnOrder", "采购退货单"),
    WAREHOUSE_RECEIVE("warehouseReceive", "仓库收货单"),
    SALES_STOCK_OUT("purchaseStockOut", "销售出库"),
    INVENTORY_ADJUST("inventoryAdjust", "库存调整"),
    TRANSFER_APPLICATION("transferApplication", "调拨申请单"),

    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    SourceTypeEnum(String type, String name) {
        this.code = type;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String type) {
        for (SourceTypeEnum sourceTypeEnum : SourceTypeEnum.values()) {
            if (type.equals(sourceTypeEnum.getCode())) {
                return sourceTypeEnum.name();
            }
        }
        return "";
    }

    public static SourceTypeEnum of(String code) {
        return Arrays.stream(SourceTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }
}
