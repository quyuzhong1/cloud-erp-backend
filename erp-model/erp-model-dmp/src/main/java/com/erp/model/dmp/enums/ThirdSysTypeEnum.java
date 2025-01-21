package com.erp.model.dmp.enums;

/**
 * 第三方系统类型
 *
 * @author hyj
 * @date 2024/5/20 12:24
 */
public enum ThirdSysTypeEnum {

    WDT("wdt", "旺店通"),


    SHOP("shop", "店铺"),
    LOGISTICS("logistics", "物流渠道"),
    WAREHOUSE("warehouse", "仓库"),
    VIRTUAL_WAREHOUSE("virtualWarehouse", "虚拟仓"),
    ;

    private String code;

    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    ThirdSysTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ThirdSysTypeEnum getByCode(String code) {
        ThirdSysTypeEnum[] values = values();
        for (ThirdSysTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
    public static String getNameByCode(String code) {
        ThirdSysTypeEnum[] values = values();
        for (ThirdSysTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value.getName();
            }
        }
        return null;
    }

    public static ThirdSysTypeEnum getByName(String name) {
        ThirdSysTypeEnum[] values = values();
        for (ThirdSysTypeEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

}
