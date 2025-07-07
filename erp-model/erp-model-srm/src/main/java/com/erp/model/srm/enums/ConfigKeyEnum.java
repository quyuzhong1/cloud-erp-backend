package com.erp.model.srm.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/27 19:13
 */
public enum ConfigKeyEnum {

    ORDER_AUTO_ACCEPT("orderAutoAccept", "订单自动接受"),
    RETURN_AUTO_CONFIRM("returnAutoConfirm", "退货自动确认"),
    PO_RECONCILIATION("poReconciliation","对账设置")
    ;

    private String code;
    private String name;

    ConfigKeyEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        ConfigKeyEnum[] stateEnums = values();
        for (ConfigKeyEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
    public static ConfigKeyEnum getEnum(String code) {
        for (ConfigKeyEnum settingEnum : ConfigKeyEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
