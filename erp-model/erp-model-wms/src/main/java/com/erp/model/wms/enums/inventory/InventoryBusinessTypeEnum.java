package com.erp.model.wms.enums.inventory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Classname: InventoryTransTypeEnum
 * @Description: 仓库存业务类型
 * @CreateTime: 2023-04-25  10:55
 * @Author: zhangchunlin
 */
public enum InventoryBusinessTypeEnum {

    /**
     * 调用方请传输code，不要传输type
     */

    INVENTORY_INIT("inventory_init", "00", "期初库存"),
    PO_RECEIVE("po_receive", "01", "采购签收（采购收货）"),

    PO_INSTOCK_REC("po_instock_rec", "02", "采购入库（有收货单）"),
    PO_INSTOCK_UNREC("po_instock_unrec", "03", "采购入库（无收货单）"),

    PO_RETURN_REP("po_return_rep", "04", "采购退货（库存退货，退货补货）"),
    PO_RETURN_REF("po_return_ref", "05", "采购退货（库存退货，退货退款）"),

    TRANSFER_APPLY("transfer_apply", "06", "调拨申请单"),
    DIRECT_ALLOCATE("direct_allocate", "07", "直接调拨单（新增）"),

    TRANSFER_OUT("transfer_out", "08", "分步式调拨调出"),
    TRANSFER_IN("transfer_in", "09", "分步式调拨调入"),

    SO_DELIVERY_NOTICE("so_delivery_notice", "10", "销售发货通知单"),
    SO_OUTSTOCK("so_outstock", "11", "销售出库冻结"),
    SO_RETURN_INSTOCK("so_return_instock", "12", "销售退货"),

    STOCKTAKING_PROFIT("stocktaking_profit_loss", "13", "盘盈"),
    STOCKTAKING_LOSS("stocktaking_profit_loss", "14", "盘亏"),

    ASSEMBLE_PICK("assemble_pick", "15", "领料出库"),
    ASSEMBLE_RETURN("assemble_return", "16", "领料入库"),
    ASSEMBLE_IN_PARENT("assemble_in_parent", "17", "加工单组装（父SKU增加）"),
    DISASSEMBLE_IN_PARENT("disassemble_in_parent", "18", "加工单拆卸（父SKU减少）"),

    OTHER_IN("other_in", "19", "其他入库"),
    OTHER_OUT("other_out", "20", "其他出库"),


    INSTOCK_FORCAST("instock_forcast", "21", "入库预报"),

    // TODO 暂不确定是否需要该业务，无法确定状态
    INVENTORY_ADJUST("inventory_adjust", "22", "库存调整单"),

    ASSEMBLE_IN_CHILDD("assemble_in_child", "23", "加工单组装（子SKU减少）"),
    DISASSEMBLE_IN_CHILD("disassemble_in_child", "24", "加工单拆卸（子SKU增加）"),

    PURCHASE_ORDER_FINISH("purchase_order_finish", "25", "采购订单结束交货"),

    DIRECT_ALLOCATE_APPLY("direct_allocate_apply", "26", "直接调拨单（调拨申请单下推）"),

    PO_RETURN_QC("po_return_qc", "27", "采购退货（质检退货，退货补货）"),
    // 自定义规则，无法固化
    PURCHASE_ORDER_CHANGE("purchase_order_change", "28", "采购订单变更"),

    PO_RETURN_QC_REF("po_return_qc_ref", "29", "采购退货（质检退货，退货退款）"),

    PO_RETURN_REP_NO_PURCHASE("po_return_rep_no_purchase", "30", "采购退货（库存退货，无采购单）"),

    WAREHOUSE_LOCATION_MOVE_INFO("warehouse_location_move_info", "31", "仓位移动（PDA功能）"),

    OTHER_IN_RETURN_GOODS("other_in_return_goods", "32", "其他入库（退货）"),

    OTHER_OUT_RETURN_GOODS("other_out_return_goods", "33", "其他出库（退货）"),

    SO_B2C_DELIVERY("so_b2c_delivery", "34", "b2c发货单"),
    SO_OUTSTOCK_USABLE("so_outstock_usable", "35", "销售出库扣可用库存"),
    DELIVERY_PUSH_TRANSFER("delivery_push_transfer", "38", "直接调拨单（发货单下推）"),
    DELIVERY_NOTICE_PUSH_TRANSFER("delivery_notice_push_transfer", "39", "直接调拨单（发货通知单下推）"),
    DELIVERY_PUSH_TRANSFER_TO_ULANZI("delivery_push_transfer_to_ulanzi", "40", "直接调拨单（头程发货单自动生成：发货仓->优蓝子中转仓）"),
    DELIVERY_PUSH_TRANSFER_FROM_ULANZI("delivery_push_transfer_from_ulanzi", "41", "直接调拨单（头程发货单自动生成：优蓝子中转仓->目的在途仓）"),
    WAREHOUSE_LOCATION_MOVE_INFO_ADD("warehouse_location_move_info_add", "42", "仓位移动（拣货单新增）"),
    WAREHOUSE_LOCATION_MOVE_INFO_SUBTRACT("warehouse_location_move_info_subtract", "43", "仓位移动（拣货单减少）"),
    SO_INFO_PUSH_TRANSFER("so_info_push_transfer", "44", "直接调拨单（销售订单下推）"),
    REQUISITION_PUSH_TRANSFER("requisition_push_transfer", "45", "直接调拨单（要货申请完成下推）"),
    FIRST_MILE_PUSH_TRANSFER("first_mile_push_transfer", "46", "直接调拨单（头程发货单下推）"),
    ASSEMBLE_IN_PARENT_FREEZE("assemble_in_parent_freeze", "47", "加工单组装（父SKU冻结增加）"),
    ASSEMBLE_IN_CHILD_FREEZE("assemble_in_child_freeze", "48", "加工单组装（子SKU冻结减少）"),
    SUBCONTRACT_RETURN_IN("subcontract_return_in", "49", "退料入库"),
    SUBCONTRACT_RETURN_OUT("subcontract_return_out", "50", "退料出库"),
    ;

    private String code;

    private String type;

    private String name;

    InventoryBusinessTypeEnum(String type, String code, String name) {
        this.type = type;
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * 根据仓库交易单据代码获取
     *
     * @param code
     * @return
     */
    public static InventoryBusinessTypeEnum getByCode(String code) {
        return Arrays.stream(InventoryBusinessTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 根据仓库交易单据类型获取
     *
     * @param type
     * @return
     */
    public static InventoryBusinessTypeEnum getByType(String type) {
        return Arrays.stream(InventoryBusinessTypeEnum.values()).filter(r -> Objects.equals(r.getType(), type)).findFirst().orElse(null);
    }


    public static List<InventoryBusinessTypeEnum> warehouseLocationMoveInfo() {
        return Arrays.asList(WAREHOUSE_LOCATION_MOVE_INFO, WAREHOUSE_LOCATION_MOVE_INFO_ADD, WAREHOUSE_LOCATION_MOVE_INFO_SUBTRACT);
    }

}
