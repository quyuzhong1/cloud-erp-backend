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

    INIT_STOCK("initStock", "期初库存"),
    WAREHOUSE_RECEIVE("warehouseReceive", "收货单"),
    PURCHASE_STOCK_IN("purchaseStockIn", "采购入库单"),
    PURCHASE_RETURN_ORDER("purchaseReturnOrder", "采购退货单"),
    TRANSFER_APPLY("transferApplication", "调拨申请单"),
    TRANSFER_INFO("transferInfo", "直接调拨单"),
    TRANSFER_OUT("transferOut", "分步式调拨出库单"),
    TRANSFER_IN("transferIn", "分步式调拨入库单"),
    SO_DELIVERY_NOTICE("soDeliveryNotice", "发货通知单"),
    SO_OUTSTOCK("soOutstock", "销售出库单"),
    SO_RETURN_INSTOCK("soReturnInstock", "销售退货入库单"),
    MACHINE_INFO("machineInfo", "加工单"),
    OTHER_INSTOCK("otherInstock", "其他入库单"),
    OTHER_OUTSTOCK("otherOutstock", "其他出库单"),
    INSTOCK_FORCAST("instockForcast", "入库预报"),
    RECEIVE_MATERIAL("receiveMaterial", "领料"),
    RETURN_MATERIAL("returnMaterial", "退料"),
    STOCKTAKING_PROFIT_LOSS("stocktakingProfitLoss", "盘盈盘亏"),
    WAREHOUSE_LOCATION_MOVE_INFO("warehouseLocationMoveInfo", "仓位移动"),
    FBA_DELIVERY("fbaDelivery", "FBA发货单"),
    SO_B2C_DELIVERY("soB2cDelivery", "b2c发货单"),
    VIRTUAL_WAREHOUSE_ALLOCATION("virtualWarehouseAllocation", "分货单"),
    REQUISITION_APPLICATION("requisitionApplication", "要货申请"),
    SO_INFO("soInfo", "B2B销售订单"),
    SO_B2C_DELIVERY_INTERCEPT("soB2cDeliveryIntercept", "b2c发货拦截单"),
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


    public static InventorySourceTypeEnum getByCode(String code) {
        return Arrays.stream(InventorySourceTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}