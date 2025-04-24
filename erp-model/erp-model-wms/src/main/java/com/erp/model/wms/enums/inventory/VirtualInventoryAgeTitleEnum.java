package com.erp.model.wms.enums.inventory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 虚拟仓库龄报表固定标题
 * @author will
 * @date 2024/12/23 15:38
 */
public enum VirtualInventoryAgeTitleEnum {

    SKU_NO("skuNo", "SKU"),
    PRODUCT_NAME("productName", "产品信息"),
    WAREHOUSE_NAME("warehouseName", "实体仓名称"),
    VIRTUAL_WAREHOUSE_CODE("virtualWarehouseCode", "虚拟仓编号"),
    VIRTUAL_WAREHOUSE_NAME("virtualWarehouseName", "虚拟仓名称"),
    VIRTUAL_QTY("virtualQty", "虚拟仓库存"),
    VIRTUAL_USABLE_QTY("virtualUsableQty", "虚拟仓可用"),
    VIRTUAL_FROZEN_QTY("virtualFrozenQty", "虚拟仓冻结"),
    FROZEN_QTY("frozenQty", "单据冻结数"),
    FROZEN_IS_DIFF("frozenIsDiff", "冻结库存差异"),
    DATE("date", "统计日期"),
    BACK_AVG_INVENTORY_AGE("backAvgInventoryAge", "平均库龄(天)"),
    AVG_INVENTORY_AGE("avgInventoryAge", "平均库龄(正)"),
    IS_DIFF("isDiff", "库龄计算差异"),


    ;
    private String code;
    private String name;

    VirtualInventoryAgeTitleEnum(String code, String name) {
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
    public static VirtualInventoryAgeTitleEnum getByCode(String code) {
        return Arrays.stream(VirtualInventoryAgeTitleEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 根据代码获取名称
     * @param code
     * @return
     */
    public static String getNameByCode(String code) {
        VirtualInventoryAgeTitleEnum inventoryAgeTitleEnum = getByCode(code);
        return Optional.ofNullable(inventoryAgeTitleEnum).map(VirtualInventoryAgeTitleEnum::getName).orElse("");
    }

}