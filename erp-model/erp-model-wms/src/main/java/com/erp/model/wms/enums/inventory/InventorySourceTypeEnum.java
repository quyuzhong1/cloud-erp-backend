package com.erp.model.wms.enums.inventory;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: InventorySourceTypeEnum
 * @Description: 库存交易单据类型
 * @CreateTime: 2023-05-05  18:15
 * @Author: zhangchunlin
 */
@NoArgsConstructor
public enum InventorySourceTypeEnum {

    INVENTORY_INIT("inventoryInit", "期初库存"),
    WAREHOUSE_RECEIVE("warehouseReceive", "采购签收单（收货单）"),
    PURCHASE_STOCK_IN("purchaseStockIn", "采购入库单"),
    PURCHASE_RETURN_ORDER("purchaseReturnOrder", "采购退货单"),
    TRANSFER_APPLY("transferApply", "调拨申请单"),
    DIRECT_TRANSFER("directTransfer", "直接调拨单"),
    STEP_TRANSFER_OUT("stepTransferOut", "分步式调出单"),
    STEP_TRANSFER_IN("stepTransferIn", "分步式调入单"),
    SHIP_NOTICE("shipNotice", "发货通知单"),
    SALES_DELIVERY_ORDER("sales_delivery_order", "销售出库单"),
    SALES_RETURN_RECEIPT("sales_return_receipt", "销售退货入库单"),
    MACHINE("machine", "加工单"),
    OTHER_IN_RECEIPT("other_in_receipt", "其他入库单"),
    OTHER_OUT_RECEIPT("other_out_receipt", "其他出库单"),
    INSTOCK_FORCAST("instock_forcast", "入库预报"),

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

    InventorySourceTypeEnum(String type, String name) {
        this.code = type;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public static InventorySourceTypeEnum of(String code) {
        return Arrays.stream(InventorySourceTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}