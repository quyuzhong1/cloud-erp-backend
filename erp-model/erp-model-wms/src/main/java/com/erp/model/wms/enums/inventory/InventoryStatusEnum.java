package com.erp.model.wms.enums.inventory;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: InventoryTransTypeEnum
 * @Description: 仓库库存状态
 * @CreateTime: 2023-04-25  11:25
 * @Author: zhangchunlin
 */
public enum InventoryStatusEnum {
    USABLE("usable", "可用"),
    FROZEN("frozen", "冻结"),
    IN_TRANSIT("inTransit", "在途"),
    WAIT_QC("waitQc", "待检"),
    DEFECTIVE("defective", "不良"), // TODO 待确认去掉
    WASTE("waste", "废品"), // TODO 待确认去掉
    LEND("lend", "外借"), // TODO 待确认去掉
    ;

    //实际库存=可用库存+冻结库存+不良库存+废品库存+外借库存，待检库存和在途库存不计入。
    @JsonValue
    @EnumValue
    private String code;

    /**
     * 名称
     */
    private String name;


    InventoryStatusEnum(String code, String name) {
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
    public static InventoryStatusEnum of(String code) {
        return Arrays.stream(InventoryStatusEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}
