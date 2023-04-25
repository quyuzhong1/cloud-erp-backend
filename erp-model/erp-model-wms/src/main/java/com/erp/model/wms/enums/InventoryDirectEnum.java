package com.erp.model.wms.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: InventoryTransTypeEnum
 * @Description: 仓库进出方向
 * @CreateTime: 2023-04-25  11:25
 * @Author: zhangchunlin
 */
public enum InventoryDirectEnum {
    INVENTORY_IN("inventory_in", "增加"),
    INVENTORY_OUT("inventory_out", "减少"),
    ;

    private String code;

    /**
     * 名称
     */
    private String name;


    InventoryDirectEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    /**
     * 根据状态代码获取
     * @param code
     * @return
     */
    public static InventoryDirectEnum of(String code) {
        return Arrays.stream(InventoryDirectEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}
