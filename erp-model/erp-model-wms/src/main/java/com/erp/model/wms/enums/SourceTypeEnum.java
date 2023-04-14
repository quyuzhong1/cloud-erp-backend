package com.erp.model.wms.enums;

public enum SourceTypeEnum {
    PURCHASE_STOCK_IN("purchaseStockIn", "采购入库单"),
    QC_BILL("qcBill", "质检单"),
    PURCHASE_RETURN_ORDER("purchaseReturnOrder", "采购退货单");

    /**
     * 类型
     */

    private String type;
    /**
     * 名称
     */
    private String name;


    SourceTypeEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static String getName(String type) {
        for (SourceTypeEnum sourceTypeEnum : SourceTypeEnum.values()) {
            if (type.equals(sourceTypeEnum.getType())) {
                return sourceTypeEnum.getType();
            }
        }
        return "";
    }
}
