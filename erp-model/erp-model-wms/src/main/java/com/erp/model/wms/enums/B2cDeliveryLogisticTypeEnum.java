package com.erp.model.wms.enums;

/**
 * b2c发货单物流类型
 */
public enum B2cDeliveryLogisticTypeEnum {
    ALL("all",  "全部"),
    TRANSIT_SHIPMENT("transitShipment",  "中转发货"),
    SELF_SHIPMENT("selfShipment",  "自发货"),
    ;

    private String code;
    private String name;

    B2cDeliveryLogisticTypeEnum(String code, String name) {

        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (B2cDeliveryLogisticTypeEnum dictBasic : B2cDeliveryLogisticTypeEnum.values()) {
            if (code.equals(dictBasic.getCode())) {
                return dictBasic.getName();
            }
        }
        return "";
    }
}
