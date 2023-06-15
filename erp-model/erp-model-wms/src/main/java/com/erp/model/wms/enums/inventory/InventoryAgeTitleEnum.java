package com.erp.model.wms.enums.inventory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 库龄报表固定标题
 * @CreateTime: 2023-06-15  17:36
 * @Author: zhangchunlin
 */
public enum  InventoryAgeTitleEnum {

    SKU_INFO("skuInfo", "产品信息"),
    SPU_NO("spuNo", "SPU型号"),
    SALE_STATUS_NAME("saleStatusName", "销售状态"),
    ORG_NAME("orgName", "库存组织"),
    WAREHOUSE_LOCATION("warehouseLocation", "仓位"),
    USABLE_INVENTORY("usableInventory", "可用库存"),
    ;
    private String code;
    private String name;

    InventoryAgeTitleEnum(String code, String name) {
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
    public static InventoryAgeTitleEnum of(String code) {
        return Arrays.stream(InventoryAgeTitleEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 根据代码获取名称
     * @param code
     * @return
     */
    public static String getNameByCode(String code) {
        InventoryAgeTitleEnum inventoryAgeTitleEnum = of(code);
        return Optional.ofNullable(inventoryAgeTitleEnum).map(InventoryAgeTitleEnum::getName).orElse("");
    }

}