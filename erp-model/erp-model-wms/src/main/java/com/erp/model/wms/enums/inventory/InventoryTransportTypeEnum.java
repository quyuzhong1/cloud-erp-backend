package com.erp.model.wms.enums.inventory;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @Classname: InventorySourceTypeEnum
 * @Description: 库存在途单据类型
 * @CreateTime: 2023-06-13  18:15
 * @Author: zhangchunlin
 */
@NoArgsConstructor
public enum InventoryTransportTypeEnum {

    PURCHASE("purchase", "采购订单"),
    TRANSFER("transfer", "调拨单"),

    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    InventoryTransportTypeEnum(String type, String name) {
        this.code = type;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public static InventoryTransportTypeEnum of(String code) {
        return Arrays.stream(InventoryTransportTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    public static String getNameByCode(String code) {
        InventoryTransportTypeEnum inventoryTransportTypeEnum = of(code);
        return Optional.ofNullable(inventoryTransportTypeEnum).map(InventoryTransportTypeEnum::getName).orElse("");
    }

}