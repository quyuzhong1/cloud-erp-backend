package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

@Getter
public enum SoB2cReturnReasonEnum implements EnumMessage {
    COLOR("COLOR","COLOR"),
    DEFECTIVE("DEFECTIVE","DEFECTIVE"),
    NOT_AS_DESCRIBED("NOT_AS_DESCRIBED","NOT_AS_DESCRIBED"),
    OTHER("OTHER","OTHER"),
    SIZE_TOO_LARGE("SIZE_TOO_LARGE","SIZE_TOO_LARGE"),
    SIZE_TOO_SMALL("SIZE_TOO_SMALL","SIZE_TOO_SMALL"),
    STYLE("STYLE","STYLE"),
    UNKNOWN("UNKNOWN","UNKNOWN"),
    UNWANTED("UNWANTED","UNWANTED"),
    WRONG_ITEM("WRONG_ITEM","WRONG_ITEM"),
    ;

    SoB2cReturnReasonEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public final String code;
    /**
     * 名称
     */
    private final String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 通过code查询
     */
    public static SoB2cReturnReasonEnum getByCode(String code){
        return Stream.of(SoB2cReturnReasonEnum.values())
                .filter(typeEnum -> typeEnum.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SoB2cReturnReasonEnum typeEnum : SoB2cReturnReasonEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }
}
