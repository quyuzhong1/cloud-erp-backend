package com.common.business.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum QueryDisplayTypeEnum implements EnumMessage {

    QUERY("query","查询条件"),
    TAB("tab","选项卡"),
    DIMENSION("dimension","维度"),
    EXPORT("export","导出"),
    ;

    private final String code;

    private final String name;

    QueryDisplayTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
