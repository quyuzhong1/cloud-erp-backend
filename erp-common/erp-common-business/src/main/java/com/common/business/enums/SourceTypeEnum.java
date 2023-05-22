package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

@NoArgsConstructor
public enum SourceTypeEnum {
    SELF_ADD("selfAdd", "手动新增"),

    //WMS
    PURCHASE_ORDER("purchaseOrder", "采购订单"),
    PURCHASE_STOCK_IN("purchaseStockIn", "采购入库单"),
    QC_BILL("qcBill", "质检单"),
    PURCHASE_RETURN_ORDER("purchaseReturnOrder", "采购退货单"),
    WAREHOUSE_RECEIVE("warehouseReceive", "仓库收货单"),
    TRANSFER_APPLICATION("transferApplication", "调拨申请单"),
    SO_RETURN_NOTICE("soReturnNotice", "销售退货通知单"),
    SO_RETURN_RECEIVE("soReturnReceive", "销售退货签收单"),
    SO_RETURN_INSTOCK("soReturnInstock", "销售退货入库单"),
    SO_DELIVERY_NOTICE("soDeliveryNotice", "销售发货通知单"),

    //OMS
    SO_RETURN("soReturn", "销售退货订单"),


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
