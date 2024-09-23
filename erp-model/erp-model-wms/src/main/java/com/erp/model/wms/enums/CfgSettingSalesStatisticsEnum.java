package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CfgSettingSalesStatisticsEnum implements EnumMessage {

    TODAY("today","今天"),
    YESTERDAY("yesterday","昨天"),
    THREE_DAYS("threeDays","近三日"),
    SEVEN_DAYS("sevenDays","近7日"),
    FOURTEEN_DAYS("fourteenDays","近14日"),
    THIRTY_DAYS("thirtyDays","近30日"),
    SIXTY_DAYS("sixtyDays","近60日"),
    NINETY_DAYS("ninetyDays","近90日")
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


    CfgSettingSalesStatisticsEnum(String code, String name) {
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
        for (CfgSettingSalesStatisticsEnum settingEnum : CfgSettingSalesStatisticsEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CfgSettingSalesStatisticsEnum getEnum(String code) {
        for (CfgSettingSalesStatisticsEnum settingEnum : CfgSettingSalesStatisticsEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
