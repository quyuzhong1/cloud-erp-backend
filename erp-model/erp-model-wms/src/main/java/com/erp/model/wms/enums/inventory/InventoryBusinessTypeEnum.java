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

    INVENTORY_INIT("inventory_init", "00","期初库存", false),
    PO_RECEIVE("po_receive", "01","采购收货（采购签收）", true),
    QC_INFO("qc_info", "02","采购质检（签收质检）", true),

    PO_INSTOCK("po_instock", "03","采购入库", true),
    PO_RETURN("po_return", "04","采购退货", true),
    INVENTORY_OUT("inventory_out", "05","库存调出", true),
    INVENTORY_IN("inventory_in", "06","库存调入", true),
    OTHER_IN("other_in", "08","其他入库", false),
    OTHER_OUT("other_out", "09","其他出库", true),

    INVENTORY_PROFIT("inventory_profit", "10","盘盈", false),
    INVENTORY_LOSS("inventory_loss", "11","盘亏", true),
    SALES_ORDER_SHIP("sales_order_ship", "14","销售出库", true),
    SALES_ORDER_RETURN("sales_order_return", "16","销售退货", false),
    ASSEMBLE_PICK("assemble_pick", "25","领料", true),
    ASSEMBLE_IN("assemble_in", "26","组装", false),
    ASSEMBLE_RETURN("assemble_return", "27","退料", false),
    INVENTORY_PREDICTION("inventory_prediction", "28","入库预报", false),
    QC_OUT("qc_out", "29","外检", true),
    INVENTOR_ALLOCATE("inventory_allocate", "30","调拨申请单", true),
    DIRECT_ALLOCATE("direct_allocate", "31","直接调拨单", null),// 无法确定状态
    SALES_SEND_GOODS("sales_send_goods", "32","销售发货通知单", true),
    DISASSEMBLE("disassemble", "33","拆卸", true),
    INVENTORY_ADJUST("inventory_adjust", "34","库存调整单", null),// 无法确定状态
    ;

    private String code;

    private String type;

    /**
     * 名称
     */
    private String name;

    private Boolean checkOutStock;


    InventoryBusinessTypeEnum(String type, String code, String name, Boolean checkOutStock) {
        this.type = type;
        this.code = code;
        this.name = name;
        this.checkOutStock = checkOutStock;
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

    public Boolean getCheckOutStock() {
        return checkOutStock;
    }

    public void setCheckOutStock(Boolean checkOutStock) {
        this.checkOutStock = checkOutStock;
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
