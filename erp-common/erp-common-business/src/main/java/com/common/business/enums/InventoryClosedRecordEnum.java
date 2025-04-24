package com.common.business.enums;

/**
 * 金蝶库存关账类型枚举
 * @author Will
 * @version 1.0
 * @date 2024/2/28 17:24
 */
public enum InventoryClosedRecordEnum {

    STK("STK", "金蝶供应链库存"),
    HS("HS", "金蝶存货核算"),
    ;

    public String code;
    public String name;

    public String code() {
        return code;
    }
    InventoryClosedRecordEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (InventoryClosedRecordEnum item : InventoryClosedRecordEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }

}
