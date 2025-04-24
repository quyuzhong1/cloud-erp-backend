package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CfgSettingSalesStatisticsEnum implements EnumMessage {

    TODAY("today","今天",1),
    YESTERDAY("yesterday","昨天",1),
    THREE_DAYS("threeDays","近三日",3),
    SEVEN_DAYS("sevenDays","近7日",7),
    FOURTEEN_DAYS("fourteenDays","近14日",14),
    THIRTY_DAYS("thirtyDays","近30日",30),
    SIXTY_DAYS("sixtyDays","近60日",60),
    NINETY_DAYS("ninetyDays","近90日",90)
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

    /**
     * 编码
     */
    private Integer num;


    CfgSettingSalesStatisticsEnum(String code, String name,Integer num) {
        this.code = code;
        this.name = name;
        this.num = num;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public Integer getNum() {
        return num;
    }

    public static String getName(String code) {
        for (CfgSettingSalesStatisticsEnum settingEnum : CfgSettingSalesStatisticsEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static Integer getNum(String code) {
        for (CfgSettingSalesStatisticsEnum settingEnum : CfgSettingSalesStatisticsEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getNum();
            }
        }
        return null;
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
