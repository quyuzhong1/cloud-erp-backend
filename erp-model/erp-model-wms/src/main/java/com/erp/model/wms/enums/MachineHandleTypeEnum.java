package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * @description: 加工单子件处理类型
 * @author Will
 * @date: 2023/8/28 17:14
 */
public enum MachineHandleTypeEnum {

    RETURN_SUPPLIER ("return_supplier", "退供应商"),
    MOVE_WAREHOUSE("move_warehouse", "移仓");

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    MachineHandleTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public static String getByCode(String code) {
        MachineHandleTypeEnum qcResultEnum  = Arrays.stream(values()).filter(p -> p.getCode().equals(code))
                .findFirst().orElse(null);
        if (qcResultEnum != null) {
            return qcResultEnum.getName();
        }
        return "";
    }
}
