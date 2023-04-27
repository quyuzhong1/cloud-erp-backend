package com.erp.model.wms.enums.inventory;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: InventoryTransTypeEnum
 * @Description: 仓库操作类型
 * @CreateTime: 2023-04-25  11:25
 * @Author: zhangchunlin
 */
public enum InventoryOperationModeEnum {
    APPROVE("approve", "审核"),
    UN_APPROVE("unApprove", "反审核"),
    AMEND("amend", "后补单"),
    ;

    private String code;

    /**
     * 名称
     */
    private String name;


    InventoryOperationModeEnum(String code, String name) {
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
    public static InventoryOperationModeEnum of(String code) {
        return Arrays.stream(InventoryOperationModeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}
