package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author hyj
 * @description: 入库类型
 * @date 2024/5/11 16:12
 */
public enum InstockStatusEnum implements EnumMessage {


    NOT_IN_STOCK("0", "未入库"),
    PARTIALLY_IN_STOCK("1", "部分入库"),
    FULLY_IN_STOCK("2", "已入库"),
    FULLY_OUT_STOCK("3", "超出入库"),
    ;

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

    InstockStatusEnum(String code, String name) {
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

        InstockStatusEnum[] enumList = InstockStatusEnum.values();
        for (InstockStatusEnum item : enumList) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "";
    }
}
