package com.common.core.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Lambda
 * @Classname RuleCompareEnum
 * @Description 规则比较枚举
 * @Date 2023-09-06 20:31
 * @Created by yl
 */
public enum RuleLogicEnum  implements EnumMessage {



    AND("&&", "且"),
    OR("||", "或"),

    ;


    private String code;

    private String name;


    RuleLogicEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (RuleLogicEnum item : RuleLogicEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
