package com.common.business.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum QueryDateTypeEnum implements EnumMessage {

    YEAR("year","年","yyyy"),
    MONTH("month","月","yyyy-MM"),
    DATE("date","日期","yyyy-MM-DD"),
    DATETIME("datetime","时间","yyyy-MM-DD HH24:MI:SS"),
    ;
    private final String code;

    private final String name;

    private final String format;

    QueryDateTypeEnum(String code, String name,String format) {
        this.code = code;
        this.name = name;
        this.format = format;
    }

}
