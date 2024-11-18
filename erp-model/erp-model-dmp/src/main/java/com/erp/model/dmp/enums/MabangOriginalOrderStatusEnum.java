package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 马帮原始订单状态
 */
public enum MabangOriginalOrderStatusEnum implements EnumMessage {
    INDISTRIBUTION(2, "配货中"),
    SHIPPED(3, "已发货"),
    FINISH(4, "已完成"),
    INVALID(5, "已作废"),
    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private Integer code;
    /**
     * 名称
     */
    private String name;

    MabangOriginalOrderStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (MabangOriginalOrderStatusEnum statusEnum : MabangOriginalOrderStatusEnum.values()) {
            if (code.equals(statusEnum.getCode().toString())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static MabangOriginalOrderStatusEnum getByCode(Integer code) {
        return Arrays.stream(MabangOriginalOrderStatusEnum.values())
                .filter(platform -> platform.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
