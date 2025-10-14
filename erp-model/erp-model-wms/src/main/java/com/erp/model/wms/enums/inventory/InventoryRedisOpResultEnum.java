package com.erp.model.wms.enums.inventory;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * 
 */
@Getter
public enum InventoryRedisOpResultEnum implements EnumMessage {
	SUCCESS("0","成功" , null , false),
	/* com.erp.model.wms.enums.inventory.InventoryRedisOpEnum.OVERRIDE_INVENTORY start */
	OVERRIDE_1("1","存在未同步事务" , InventoryRedisOpResultEnum.OVERRIDE , true),
	/* com.erp.model.wms.enums.inventory.InventoryRedisOpEnum.OVERRIDE_INVENTORY end*/
    ;

    InventoryRedisOpResultEnum(String code, String name , InventoryRedisOpEnum inventoryRedisOpEnum , boolean isRetry) {
        this.code = code;
        this.name = name;
        this.inventoryRedisOpEnum = inventoryRedisOpEnum;
        this.isRetry = isRetry;
    }
    
    private static final InventoryRedisOpEnum OVERRIDE = InventoryRedisOpEnum.OVERRIDE;
    
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
     * 操作类型
     */
    private InventoryRedisOpEnum inventoryRedisOpEnum;
    
    /**
     * 是否可重试
     */
    private boolean isRetry;

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
    public static InventoryRedisOpResultEnum getByCode(String code) {
        return Stream.of(InventoryRedisOpResultEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
    
    public static InventoryRedisOpResultEnum getByOpAndCode(InventoryRedisOpEnum inventoryRedisOpEnum , String code) {
    	if(InventoryRedisOpResultEnum.SUCCESS.getCode().equals(code)) {
    		return InventoryRedisOpResultEnum.SUCCESS;
    	}
    	return Stream.of(InventoryRedisOpResultEnum.values())
    			.filter(e -> e.getCode().equalsIgnoreCase(code) && e.inventoryRedisOpEnum == inventoryRedisOpEnum)
    			.findFirst()
    			.orElse(null);
    }
}
