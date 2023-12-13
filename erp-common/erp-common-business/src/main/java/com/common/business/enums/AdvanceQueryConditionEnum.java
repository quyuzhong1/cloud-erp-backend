package com.common.business.enums;

public enum AdvanceQueryConditionEnum {

    GT(">", "大于"),
    GE(">=", "大于等于"),
    LT("<", "小于"),
    LE("<=", "小于等于"),
    EQ("=", "等于"),
    NE("!=", "不等于"),
    CONTAINS("in", "包含"),
    NOT_CONTAINS("not in", "不包含"),
    LIKE("like", "属于"),
    NOT_LIKE("not like", "不属于"),
    IS_NULL("is null", "为空"),
    NOT_NULL("is not null", "不为空"),
    ;
    private String code;

    private String name;

    AdvanceQueryConditionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
