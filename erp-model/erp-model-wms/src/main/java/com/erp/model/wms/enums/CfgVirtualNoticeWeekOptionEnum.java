package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 通知时间类型
 * @Auther will
 * @Date 2025/2/12 16:13
 */
public enum CfgVirtualNoticeWeekOptionEnum implements EnumMessage {

    MONDAY("monday","星期一"),
    TUESDAY("tuesday","星期二"),
    WEDNESDAY("wednesday","星期三"),
    THURSDAY("thursday","星期四"),
    FRIDAY("friday","星期五"),
    SATURDAY("saturday","星期六"),
    SUNDAY("sunday","星期日"),

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


    CfgVirtualNoticeWeekOptionEnum(String code, String name) {
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
        for (CfgVirtualNoticeWeekOptionEnum settingEnum : CfgVirtualNoticeWeekOptionEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CfgVirtualNoticeWeekOptionEnum getEnum(String code) {
        for (CfgVirtualNoticeWeekOptionEnum settingEnum : CfgVirtualNoticeWeekOptionEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
