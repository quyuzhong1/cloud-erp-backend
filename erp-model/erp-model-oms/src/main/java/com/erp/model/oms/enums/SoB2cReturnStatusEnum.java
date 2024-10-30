package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

@Getter
public enum SoB2cReturnStatusEnum implements EnumMessage {
    TO_BE_RETURNED("toBeReturned","待退货"),
    RETURNED("returned","已退货"),
    ;

    SoB2cReturnStatusEnum(String code, String name) {
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
    public static SoB2cReturnStatusEnum getByCode(String code){
        return Stream.of(SoB2cReturnStatusEnum.values())
                .filter(typeEnum -> typeEnum.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SoB2cReturnStatusEnum typeEnum : SoB2cReturnStatusEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }
}
