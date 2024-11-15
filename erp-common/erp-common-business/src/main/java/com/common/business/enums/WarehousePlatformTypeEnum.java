package com.common.business.enums;

import lombok.Getter;

/**
 * 第三方仓库平台类型
 */
@Getter
public enum WarehousePlatformTypeEnum {

    OVERSEAS_WAREHOUSE("overseasWarehouse","海外仓"),
    FBA_INVENTORY("fbaInventory","FBA仓库")
    ;
    public final String code;
    public final String name;

    public String code() {
        return code;
    }
    WarehousePlatformTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (WarehousePlatformTypeEnum item : WarehousePlatformTypeEnum.values()) {
            if (code.equals(item.getCode())) {
                item.getName();
            }
        }
        return "";
    }
}
