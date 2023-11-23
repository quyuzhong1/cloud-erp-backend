package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 入库单状态枚举
 */
public enum InstockStatusEnum implements EnumMessage  {
    TO_BE_SHIPPED("toBeShipped", "待发货"),
    TO_BE_SIGNED("toBeSigned", "待签收"),
    PARTIAL_SIGNED("partialSigned", "部分签收"),
    SIGNED("signed", "已签收"),
    CANCELED("canceled", "已取消"),
    ABNORMAL("abnormal", "异常")
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    InstockStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }
    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (InstockStatusEnum item : InstockStatusEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static InstockStatusEnum getByCode(String code){
        return Arrays.stream(values()).filter(a -> a.getCode().equals(code))
                .findFirst().orElse(null);
    }

}
