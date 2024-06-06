package com.erp.model.wms.enums.inventory;

import java.util.Arrays;
import java.util.Objects;

/**
 * 虚拟仓库库存业务
 * @author will
 * @date 2024/6/4 10:58
 */
public enum VirtualInventoryBusinessTypeEnum {

    /**
     * 调用方请传输code，不要传输type
     */

    SO_OUTSTOCK_USABLE("so_outstock_usable", "35","销售出库扣可用库存"),

    ;

    private String code;

    private String type;

    private String name;

    VirtualInventoryBusinessTypeEnum(String type, String code, String name) {
        this.type = type;
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * 根据仓库交易单据代码获取
     * @param code
     * @return
     */
    public static VirtualInventoryBusinessTypeEnum getByCode(String code) {
        return Arrays.stream(VirtualInventoryBusinessTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 根据仓库交易单据类型获取
     * @param type
     * @return
     */
    public static VirtualInventoryBusinessTypeEnum getByType(String type) {
        return Arrays.stream(VirtualInventoryBusinessTypeEnum.values()).filter(r -> Objects.equals(r.getType(), type)).findFirst().orElse(null);
    }



}
