package com.erp.model.wms.enums.inventory;

import com.common.core.constant.EnumMessage;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: InventoryTransTypeEnum
 * @Description: 仓库操作类型
 * @CreateTime: 2023-04-25  11:25
 * @Author: zhangchunlin
 */
public enum InventoryOperationModeEnum implements EnumMessage {
    APPROVE("approve", "审核"),
    UN_APPROVE("unApprove", "反审核"),
    // AMEND("amend", "后补单"),
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

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }


    /**
     * 根据代码获取
     * @param code
     * @return
     */
    public static InventoryOperationModeEnum getByCode(String code) {
        return Arrays.stream(InventoryOperationModeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}
