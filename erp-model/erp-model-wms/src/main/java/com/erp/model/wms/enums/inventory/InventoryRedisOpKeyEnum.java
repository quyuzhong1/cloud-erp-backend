package com.erp.model.wms.enums.inventory;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * @author Lambda
 * @Classname DeliverTypeEnum
 * @Description 发货类型枚举
 * @Date 2023-12-29 10:30
 * @Created by yl
 */
@Getter
public enum InventoryRedisOpKeyEnum implements EnumMessage {
	OVERRIDE("override","重算流水"),
	CURRENT("current","当前流水"),
    ;

    InventoryRedisOpKeyEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
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
    

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * 通过code查询
     * DeliverTypeEnum
     * 枚举
     */
    public static InventoryRedisOpKeyEnum getByCode(String code) {
        return Stream.of(InventoryRedisOpKeyEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
    
    public static String getKey(InventoryRedisOpKeyEnum inventoryRedisOpKeyEnum , String inventoryId) {
    	return Arrays.asList("inventory" , inventoryRedisOpKeyEnum.getCode() , inventoryId).stream().collect(Collectors.joining(":"));
    }
}
