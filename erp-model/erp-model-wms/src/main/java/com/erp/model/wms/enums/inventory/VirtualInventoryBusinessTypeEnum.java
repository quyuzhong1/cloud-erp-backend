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
     * 调拨
     */
    TRANSFER_USABLE("transfer_usable", "01","调拨，当前仓可用减少，目的仓增加"),
    /**
     * 出库
     */
    OUT_USABLE("out_usable", "02","出库，当前仓可用减少"),
    /**
     * 入库
     */
    IN_USABLE("in_usable", "03","入库，当前仓可用增加"),

    /**
     * b2c发货单，减可用，加冻结
     */
    SO_B2C_DELIVERY("so_b2c_delivery", "04","b2c发货单"),

    /**
     * 发货通知单，减可用，加冻结
     */
    SO_DELIVERY_NOTICE("so_delivery_notice", "05","发货通知单"),

    /**
     * 销售出库单，减冻结
     */
    SO_OUT_STOCK("so_out_stock", "06","销售出库单"),
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
