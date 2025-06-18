package com.common.business.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum DynamicDataSourceTypeEnum implements EnumMessage {

	POSTGRES("postgres","postgres数据源"),
	DORIS("doris","doris数据源"),
    ;

    private final String code;

    private final String name;

    DynamicDataSourceTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
