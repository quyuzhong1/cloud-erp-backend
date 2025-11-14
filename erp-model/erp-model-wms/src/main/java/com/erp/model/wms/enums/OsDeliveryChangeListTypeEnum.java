package com.erp.model.wms.enums;

public enum OsDeliveryChangeListTypeEnum {
    WAIT_SUBMIT("waitSubmit", "待提交"),
    TO_BE_APPROVE("toBeApprove", "待审核"),
    PACKING_COMPLETED("packingCompleted", "打包完成"),
    UN_SHIPPED("unShipped", "待发货"),
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
