package com.erp.model.wms.enums;

public enum OsDeliveryChangeListTypeEnum {
    TO_BE_APPROVE("toBeApprove", "待审批"),
    APPROVE("approve", "审核通过"),
    REJECT("reject", "不通过"),
    COMPLETE_SHIPMENT("completeShipment", "已发货"),
    ;


    private String code;
    private String name;

    OsDeliveryChangeListTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
}
