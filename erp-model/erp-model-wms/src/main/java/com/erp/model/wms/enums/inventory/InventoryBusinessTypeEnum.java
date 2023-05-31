package com.erp.model.wms.enums.inventory;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: InventoryTransTypeEnum
 * @Description: 仓库存业务类型
 * @CreateTime: 2023-04-25  10:55
 * @Author: zhangchunlin
 */
public enum InventoryBusinessTypeEnum {

    /**
     * 调用方需请传输code，不要传输type
     */

    INVENTORY_INIT("inventory_init", "00","期初库存"),
    PO_RECEIVE("po_receive", "01","采购签收（采购收货）"),

    PO_INSTOCK_REC("po_instock_rec", "02","采购入库（有收货单）"),
    PO_INSTOCK_UNREC("po_instock_unrec", "03","采购入库（无收货单）"),

    PO_RETURN_REP("po_return_rep", "04","采购退货（库存退货，退货补货）"),
    PO_RETURN_REF("po_return_ref", "05","采购退货（库存退货，退货退款）"),

    TRANSFER_APPLY("transfer_apply", "06","调拨申请单"),
    DIRECT_ALLOCATE("direct_allocate", "07","直接调拨单（新增）"),

    TRANSFER_OUT("transfer_out", "08","分步式调拨调出"),
    TRANSFER_IN("transfer_in", "09","分步式调拨调入"),

    SO_DELIVERY_NOTICE("so_delivery_notice", "10","销售发货通知单"),
    SO_OUTSTOCK("so_outstock", "11","销售出库"),
    SO_RETURN_INSTOCK("so_return_instock", "12","销售退货"),

    INVENTORY_PROFIT("inventory_profit", "13","盘盈"),
    INVENTORY_LOSS("inventory_loss", "14","盘亏"),

    ASSEMBLE_PICK("assemble_pick", "15","领料"),
    ASSEMBLE_RETURN("assemble_return", "16","退料"),
    ASSEMBLE_IN_PARENT("assemble_in_parent", "17","加工单组装（父SKU增加）"),
    DISASSEMBLE_IN_PARENT("disassemble_in_parent", "18","加工单拆卸（父SKU减少）"),

    OTHER_IN("other_in", "19","其他入库"),
    OTHER_OUT("other_out", "20","其他出库"),


    INSTOCK_FORCAST("instock_forcast", "21","入库预报"),

    INVENTORY_ADJUST("inventory_adjust", "22","库存调整单"),// TODO 暂不确定是否需要该业务，无法确定状态

    ASSEMBLE_IN_CHILDD("assemble_in_child", "23","加工单组装（子SKU减少）"),
    DISASSEMBLE_IN_CHILD("disassemble_in_child", "24","加工单拆卸（子SKU增加）"),

    PURCHASE_ORDER_FINISH("purchase_order_finish", "25","采购订单结束交货"),

    DIRECT_ALLOCATE_APPLY("direct_allocate_apply", "26","直接调拨单（调拨申请单下推）"),

    PO_RETURN_QC("po_return_qc", "27","采购退货（质检退货，退货补货）"),

    PURCHASE_ORDER_CHANGE("purchase_order_change", "28","采购订单变更"), // 自定义规则，无法固化
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
     * @param code
     * @return
     */
    public static InventoryBusinessTypeEnum of(String code) {
        return Arrays.stream(InventoryBusinessTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 根据仓库交易单据类型获取
     * @param type
     * @return
     */
    public static InventoryBusinessTypeEnum ofType(String type) {
        return Arrays.stream(InventoryBusinessTypeEnum.values()).filter(r -> Objects.equals(r.getType(), type)).findFirst().orElse(null);
    }



}
