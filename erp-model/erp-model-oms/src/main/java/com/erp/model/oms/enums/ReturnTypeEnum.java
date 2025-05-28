package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

@Getter
public enum ReturnTypeEnum implements EnumMessage {
    CUSTOMER_RETURNS("customerReturns","买家退货"),
    RETURNS_FROM_SERVICE_PROVIDERS("returnsFromServiceProviders","服务商退件"),
    CLAIM("claim","认领"),
    DEDUCTION("refund","退货退款"),
    REPLENISHMENT("replenishment","退货补货")
    ;

    ReturnTypeEnum(String code, String name) {
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
    public static ReturnTypeEnum getByCode(String code){
        return Stream.of(ReturnTypeEnum.values())
                .filter(typeEnum -> typeEnum.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (ReturnTypeEnum typeEnum : ReturnTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }
    public static String getCode(String name) {
        if(StringUtils.isBlank(name)) {
            return "";
        }
        for (ReturnTypeEnum returnTypeEnum : ReturnTypeEnum.values()) {
            if (name.equals(returnTypeEnum.getName())) {
                return returnTypeEnum.getCode();
            }
        }
        return "";
    }
}
