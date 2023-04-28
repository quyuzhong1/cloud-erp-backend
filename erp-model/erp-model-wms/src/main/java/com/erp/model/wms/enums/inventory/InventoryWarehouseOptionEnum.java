package com.erp.model.wms.enums.inventory;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: InventoryTransTypeEnum
 * @Description: 库存交易仓库类型
 * @CreateTime: 2023-04-25  11:25
 * @Author: zhangchunlin
 */
public enum InventoryWarehouseOptionEnum {
    WAREHOUSE_CURRENT("current", "当前仓"),
    WAREHOUSE_TARGET("target", "目的仓"),
    ;

    private String code;

    /**
     * 名称
     */
    private String name;


    InventoryWarehouseOptionEnum(String code, String name) {
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
    public static InventoryWarehouseOptionEnum of(String code) {
        return Arrays.stream(InventoryWarehouseOptionEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}
