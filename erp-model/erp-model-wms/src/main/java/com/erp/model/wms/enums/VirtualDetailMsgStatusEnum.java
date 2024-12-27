package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VirtualDetailMsgStatusEnum implements EnumMessage {

    WAIT_HANDLE("waitHandle","待处理"),
    SUCCESS("success","成功"),
    FAIL("fail","失败"),
    DOING("doing","进行中")
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


    VirtualDetailMsgStatusEnum(String code, String name) {
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

    public static String getName(String code) {
        for (VirtualDetailMsgStatusEnum settingEnum : VirtualDetailMsgStatusEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static VirtualDetailMsgStatusEnum getEnum(String code) {
        for (VirtualDetailMsgStatusEnum settingEnum : VirtualDetailMsgStatusEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
