package com.erp.model.wms.enums.inventory;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: InventoryBizTypeEnum
 * @Description: TODO
 * @CreateTime: 2023-05-04  15:09
 * @Author: zhangchunlin
 */
public enum InventoryBizTypeEnum {

    IN_OUT_STOCK("inOut", "出入库"),
    TRANSFER_STOCK("transfer", "调拨"),
    ;

    private String code;

    /**
     * 名称
     */
    private String name;


    InventoryBizTypeEnum(String code, String name) {
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
