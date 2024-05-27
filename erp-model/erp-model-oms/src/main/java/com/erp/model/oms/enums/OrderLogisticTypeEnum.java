package com.erp.model.oms.enums;

public enum OrderLogisticTypeEnum {
    PLATFORM_WAREHOUSE("platformWarehouse",  "平台仓"),
    TRANSIT_WAREHOUSE("transitWarehouse",  "中转仓"),
    SELF_SHIPMENT("selfShipment",  "自发货"),
    ;

    private String code;
    private String name;

    OrderLogisticTypeEnum(String code, String name) {

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
        for (OrderLogisticTypeEnum dictBasic : OrderLogisticTypeEnum.values()) {
            if (code.equals(dictBasic.getCode())) {
                return dictBasic.getName();
            }
        }
        return "";
    }
}
