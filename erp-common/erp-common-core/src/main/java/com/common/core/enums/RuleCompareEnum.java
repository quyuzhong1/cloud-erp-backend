package com.common.core.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Lambda
 * @Classname RuleCompareEnum
 * @Description 规则比较枚举
 * @Date 2023-09-06 20:31
 * @Created by yl
 */
public enum RuleCompareEnum implements EnumMessage {
    GT(">", "大于"),
    GE(">=", "大于等于"),
    LT("<", "小于"),
    LE("<=", "小于等于"),
    EQ("==", "等于"),
    NE("!=", "不等于"),
    CONTAINS("contains", "在列表"),
    NOT_CONTAINS("notContains", "不在列表"),
    IS_NULL("isEmpty", "为空"),
    NOT_NULL("notEmpty", "不为空"),
    STARTS_WITH("startsWith", "以...开头"),
    ;


    private String code;

    private String name;


    RuleCompareEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static RuleCompareEnum getByCode(String operator) {
        for (RuleCompareEnum item : RuleCompareEnum.values()) {
            if (operator.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (RuleCompareEnum item : RuleCompareEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
