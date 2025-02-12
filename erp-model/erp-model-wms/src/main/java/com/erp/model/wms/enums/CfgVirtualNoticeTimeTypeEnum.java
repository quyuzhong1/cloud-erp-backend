package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 通知时间类型
 * @Auther will
 * @Date 2025/2/12 16:13
 */
public enum CfgVirtualNoticeTimeTypeEnum implements EnumMessage {

    BY_DAY("byDay","按天"),
    BY_WEEK("byWeek","按周"),
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


    CfgVirtualNoticeTimeTypeEnum(String code, String name) {
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
        for (CfgVirtualNoticeTimeTypeEnum settingEnum : CfgVirtualNoticeTimeTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CfgVirtualNoticeTimeTypeEnum getEnum(String code) {
        for (CfgVirtualNoticeTimeTypeEnum settingEnum : CfgVirtualNoticeTimeTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
