package com.erp.model.wms.enums.inventory;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: InventoryTransTypeEnum
 * @Description: 仓库进出方向
 * @CreateTime: 2023-04-25  11:25
 * @Author: zhangchunlin
 */
public enum InventoryModeEnum {
    IN_STOCK("1", "增加"),
    OUT_STOCK("-1", "减少"),
    ;

    private String code;

    /**
     * 名称
     */
    private String name;


    InventoryModeEnum(String code, String name) {
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
     * 根据代码获取
     * @param code
     * @return
     */
    public static InventoryModeEnum of(String code) {
        return Arrays.stream(InventoryModeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}
