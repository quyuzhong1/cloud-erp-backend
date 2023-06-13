package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/15 18:12
 */
public enum MachineTypeEnum implements EnumMessage {


    ORDINARY("ordinary", "普通加工单"),
    OUTSOURCING("outsourcing", "委外加工单");

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

    MachineTypeEnum(String code, String name) {
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

    public static String getByCode(String code) {

        MachineTypeEnum[] enumList = MachineTypeEnum.values();
        for (MachineTypeEnum item : enumList) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "";
    }
}
