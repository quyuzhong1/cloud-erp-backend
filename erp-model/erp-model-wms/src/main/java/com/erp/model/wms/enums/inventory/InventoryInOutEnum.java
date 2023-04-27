package com.erp.model.wms.enums.inventory;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: InventoryInOutEnum
 * @Description: TODO
 * @CreateTime: 2023-04-26  15:09
 * @Author: zhangchunlin
 */
public enum InventoryInOutEnum {

    IN_STOCK("in", "入库"),
    OUT_STOCK("out", "出库"),
    ;

    private String code;

    /**
     * 名称
     */
    private String name;


    InventoryInOutEnum(String code, String name) {
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
