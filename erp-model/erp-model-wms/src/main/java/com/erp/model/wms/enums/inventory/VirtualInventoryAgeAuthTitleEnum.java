package com.erp.model.wms.enums.inventory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 虚拟仓库龄报表固定标题（需要权限控制的标题 ）
 * @author will
 * @date 2024/12/23 15:38
 */
public enum VirtualInventoryAgeAuthTitleEnum {

    FROZEN_QTY("frozenQty", "单据冻结数"),
    FROZEN_IS_DIFF("frozenIsDiff", "冻结库存差异"),
    AVG_INVENTORY_AGE("avgInventoryAge", "平均库龄(正)"),
    IS_DIFF("isDiff", "库龄计算差异"),


    ;
    private String code;
    private String name;

    VirtualInventoryAgeAuthTitleEnum(String code, String name) {
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
    public static VirtualInventoryAgeAuthTitleEnum getByCode(String code) {
        return Arrays.stream(VirtualInventoryAgeAuthTitleEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 根据代码获取名称
     * @param code
     * @return
     */
    public static String getNameByCode(String code) {
        VirtualInventoryAgeAuthTitleEnum inventoryAgeTitleEnum = getByCode(code);
        return Optional.ofNullable(inventoryAgeTitleEnum).map(VirtualInventoryAgeAuthTitleEnum::getName).orElse("");
    }

}