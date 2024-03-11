package com.erp.model.oms.enums;

/**
 * 美客多发货类型
 */
public enum  MercadoOrderDeliveryTypeEnum {

    FULFILLMENT("fulfillment",  "平台仓发货（fulfillment）","平台仓"),
    DROP_OFF("dropOff",  "中转仓发货（drop_off）","中转仓"),
    CROSS_DOCKING("crossDocking",  "中转仓发货（cross_docking）","中转仓"),
    DEFAULT("default",  "自发货（default）","自发货"),
    ;

    private String code;
    private String name;
    private String displayName;

    MercadoOrderDeliveryTypeEnum(String code, String name,String displayName) {

        this.code = code;
        this.name = name;
        this.displayName = displayName;
    }


    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (MercadoOrderDeliveryTypeEnum dictBasic : MercadoOrderDeliveryTypeEnum.values()) {
            if (code.equals(dictBasic.getCode())) {
                return dictBasic.getName();
            }
        }
        return "";
    }
}
