package com.erp.model.wms.enums.inventory;

import lombok.Getter;

/**
 * 即时库存查询类别
 * @date 2024-06-04
 * @author tanmujin
 */
@Getter
public enum InventorySearchDimensionEnum {
    WAREHOUSE("warehouse", "仓库"),
    WAREHOUSE_AREA("warehouseArea", "库区"),
    WAREHOUSE_LOCATION("warehouseLocation", "仓位");

    private String code;
    private String name;

    InventorySearchDimensionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
