package com.erp.model.wms.enums.inventory;

import java.util.EnumMap;
import java.util.stream.Stream;

import org.springframework.data.redis.core.script.DefaultRedisScript;

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
public enum InventoryRedisOpEnum implements EnumMessage {
	OVERRIDE_INVENTORY("overrideInventory","重算流水" , "local delimiter = '@@'; local i = 0; local key = ARGV[1]; local qty = ARGV[2]; local value = redis.call('get',key); if value == 0 or value == false then redis.call('set',key,qty); else for sku in string.gmatch(value, '([^' .. delimiter .. ']+)') do i = i + 1; end if i > 1 then return '1' else redis.call('set',key,qty); end end return '0';"),
    ;

    InventoryRedisOpEnum(String code, String name , String luaScript) {
        this.code = code;
        this.name = name;
        this.luaScript = luaScript;
    }
    
    private static EnumMap<InventoryRedisOpEnum, DefaultRedisScript<String>> opRedisScript;
    static {
    	opRedisScript = new EnumMap<>(InventoryRedisOpEnum.class);
    	InventoryRedisOpEnum[] values = InventoryRedisOpEnum.values();
    	for(InventoryRedisOpEnum v : values) {
    		DefaultRedisScript<String> defaultRedisScript = new DefaultRedisScript<>();
        	defaultRedisScript.setResultType(String.class);
        	defaultRedisScript.setScriptText(v.luaScript);
        	opRedisScript.put(v, defaultRedisScript);
    	}
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
    
    /**
     * lua脚本
     */
    private String luaScript;

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
    public static InventoryRedisOpEnum getByCode(String code) {
        return Stream.of(InventoryRedisOpEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
    
    public static DefaultRedisScript<String> getDefaultRedisScript(InventoryRedisOpEnum inventoryRedisOpEnum){
    	return opRedisScript.get(inventoryRedisOpEnum);
    }
}
