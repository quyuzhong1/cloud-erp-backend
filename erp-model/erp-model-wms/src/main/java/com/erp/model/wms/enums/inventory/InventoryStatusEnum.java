package com.erp.model.wms.enums.inventory;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: InventoryStatusEnum
 * @Description: 仓库库存状态
 * @CreateTime: 2023-04-25  11:25
 * @Author: zhangchunlin
 */
public enum InventoryStatusEnum {
    USABLE("usable", "可用", Boolean.TRUE),
    FROZEN("frozen", "冻结", Boolean.TRUE),
    IN_TRANSIT("inTransit", "在途", Boolean.FALSE),
    WAIT_QC("waitQc", "待检", Boolean.FALSE),
    ;

    //实际库存=可用库存+冻结库存，待检库存和在途库存不计入。
    @JsonValue
    @EnumValue
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 是否控制库位
     * @return
     */
    private Boolean controlLocation;

    public Boolean getControlLocation() {
        return controlLocation;
    }

    public void setControlLocation(Boolean controlLocation) {
        this.controlLocation = controlLocation;
    }


    InventoryStatusEnum(String code, String name, Boolean controlLocation) {
        this.code = code;
        this.name = name;
        this.controlLocation = controlLocation;
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
    public static InventoryStatusEnum of(String code) {
        return Arrays.stream(InventoryStatusEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}
