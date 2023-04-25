package com.erp.model.wms.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: InventoryTransTypeEnum
 * @Description: 仓库交易业务单据
 * @CreateTime: 2023-04-25  10:55
 * @Author: zhangchunlin
 */
public enum InventoryTransTypeEnum {

    INVENTORY_INIT("inventory_init", "00","库存初始化"),
    PO_RECEIVE("po_receive", "01","采购收货"),
    QC_INFO("qc_info", "02","采购质检"),
    PO_INSTOCK("po_instock", "03","采购入库"),
    PO_RETURN("po_return", "04","采购退货"),
    INVENTORY_OUT("inventory_out", "05","库存调出"),
    INVENTORY_IN("inventory_in", "06","库存调入"),
    OTHER_IN("other_in", "08","其他入库"),
    OTHER_OUT("other_out", "09","其他出库"),

    INVENTORY_PROFIT("inventory_profit", "10","盘盈"),
    INVENTORY_LOSS("inventory_loss", "11","盘亏"),
    SALES_ORDER_SHIP("sales_order_ship", "14","销售出库"),
    SALES_ORDER_RETURN("sales_order_return", "16","销售退货"),
    ASSEMBLE_PICK("assemble_pick", "25","领料"),
    ASSEMBLE_IN("assemble_in", "26","组装"),
    ASSEMBLE_RETURN("assemble_return", "27","退料"),
    ;

    private String code;

    private String type;

    /**
     * 名称
     */
    private String name;


    InventoryTransTypeEnum(String type, String code, String name) {
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
    public static InventoryTransTypeEnum of(String code) {
        return Arrays.stream(InventoryTransTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 根据仓库交易单据类型获取
     * @param type
     * @return
     */
    public static InventoryTransTypeEnum ofType(String type) {
        return Arrays.stream(InventoryTransTypeEnum.values()).filter(r -> Objects.equals(r.getType(), type)).findFirst().orElse(null);
    }

}
