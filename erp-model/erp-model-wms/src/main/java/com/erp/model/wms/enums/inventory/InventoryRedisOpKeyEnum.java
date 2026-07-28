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
	HISTORY("history","历史库存"),
	TRANSACTION("transaction","库存交易"),
	/** 仓+SKU 实体库存汇总镜像（TRY 阶段 Lua 校验通过后写入，commit/rollback 成对清理） */
	WHSKU_ENTITY("whsku:entity","仓SKU实体汇总"),
	/** 仓+SKU 未分配出库预占（TRY 阶段写入，commit/rollback 成对释放） */
	WHSKU_UNALLOC_RESERVE("whsku:unalloc:reserve","仓SKU未分配预占"),
	/** 仓+SKU 未分配 PG 路径并发锁（与 reserve 同维度，非 Redis TRY 时串行校验+扣减） */
	WHSKU_UNALLOC_LOCK("whsku:unalloc:lock","仓SKU未分配并发锁"),
	/** 仓+SKU 虚拟已分配诊断镜像（TRY 写入阶段落库，校验以 ARGV virtualQty 为准） */
	WHSKU_VIRTUAL("whsku:virtual","仓SKU虚拟已分配镜像"),
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

    /**
     * 仓+SKU 维度 Redis key（实体汇总 / 未分配预占 / 虚拟已分配镜像）。
     *
     * @param inventoryRedisOpKeyEnum 键类型，支持 {@link #WHSKU_ENTITY}、{@link #WHSKU_UNALLOC_RESERVE}、{@link #WHSKU_UNALLOC_LOCK}、{@link #WHSKU_VIRTUAL}
     * @param warehouseId             仓库 ID
     * @param skuId                   SKU ID
     * @return Redis key
     */
    public static String getWhSkuKey(InventoryRedisOpKeyEnum inventoryRedisOpKeyEnum, String warehouseId, String skuId) {
        return Arrays.asList("inventory", inventoryRedisOpKeyEnum.getCode(), warehouseId, skuId).stream()
                .collect(Collectors.joining(":"));
    }
}
