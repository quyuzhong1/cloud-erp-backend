package com.erp.model.bi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum  CompletionRateRankingEnum implements EnumMessage {
    SALES_AMOUNT("salesAmount","销售额完成率"),
    FINANCE_SALES_AMOUNT("financeSalesAmount","财务销售额完成率"),
    ;

    CompletionRateRankingEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public String code;
    /**
     * 名称
     */
    private String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }


    public static String getNameByCode(String code) {
        CompletionRateRankingEnum[] enums = values();
        for (CompletionRateRankingEnum typeEnum : enums) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum.getName();
            }
        }
        return null;
    }

    public static CompletionRateRankingEnum getEnumByCode(String code) {
        CompletionRateRankingEnum[] enums = values();
        for (CompletionRateRankingEnum typeEnum : enums) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
