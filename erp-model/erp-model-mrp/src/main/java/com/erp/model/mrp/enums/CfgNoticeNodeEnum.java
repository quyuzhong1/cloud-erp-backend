package com.erp.model.mrp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CfgNoticeNodeEnum {

    CONFIRM("confirm","确认发货建议"),
    GENERATE("generate","生成发货建议")
    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private final String code;
    /**
     * 名称
     */
    private final String name;


    public static String getName(String code) {
        for (CfgNoticeNodeEnum settingEnum : CfgNoticeNodeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static CfgNoticeNodeEnum getEnum(String code) {
        for (CfgNoticeNodeEnum settingEnum : CfgNoticeNodeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
