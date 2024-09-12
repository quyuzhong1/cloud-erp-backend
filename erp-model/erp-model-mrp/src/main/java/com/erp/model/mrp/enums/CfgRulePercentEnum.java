package com.erp.model.mrp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 百分比枚举
 * @author will
 * @date 2024/9/12 10:20
 */
public enum CfgRulePercentEnum implements EnumMessage {

    THREE_DAYS_RATIO("threeDaysRatio", "3天日均"),
    SEVEN_DAYS_RATIO("sevenDaysRatio", "7天日均"),
    FOURTEEN_DAYS_RATIO("fourteenDaysRatio", "14天日均"),
    THIRTY_DAYS_RATIO("thirtyDaysRatio", "30日均"),
    SIXTY_DAYS_RATIO("sixtyDaysRatio", "60日均"),
    NINETY_DAYS_RATIO("ninetyDaysRatio", "90日均"),
    ONE_HUNDRED_EIGHTY_DAYS_RATIO("oneHundredEightyDaysRatio", "180天日均"),
    TWO_HUNDRED_SEVENTY_DAYS_RATIO("twoHundredSeventyDaysRatio", "270天日均"),
    THREE_HUNDRED_SIXTY_DAYS_RATIO("threeHundredSixtyDaysRatio", "360天日均"),

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

    CfgRulePercentEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgRulePercentEnum statusEnum : CfgRulePercentEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
