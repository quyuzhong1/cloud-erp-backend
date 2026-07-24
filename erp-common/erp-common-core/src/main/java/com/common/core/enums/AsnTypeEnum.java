package com.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * asnType
 *
 * @author lei
 */
@Getter
@AllArgsConstructor
public enum AsnTypeEnum {

    SUPPLIER_RECEIPT("SUPPLIER_RECEIPT", "供应商收货"),
    RETURN("RETURN", "退货"),
    ;

    @EnumValue
    private final String code;
    private final String name;

    public static AsnTypeEnum getByCode(String code) {
        return Arrays.stream(values()).filter(value -> value.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
