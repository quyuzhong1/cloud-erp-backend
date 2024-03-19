package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * 头程发货单中转状态
 */
public enum FmDeliveryDeclareStatusEnum implements EnumMessage {
    NONE("none","无需生成"),
    WAIT("wait","未生成"),
    FINISH("finish","已生成"),
    ;
    FmDeliveryDeclareStatusEnum(String code, String name) {
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
    public static FmDeliveryDeclareStatusEnum getByCode(String code){
        return Stream.of(FmDeliveryDeclareStatusEnum.values())
                .filter(typeEnum -> typeEnum.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (FmDeliveryDeclareStatusEnum statusEnum : FmDeliveryDeclareStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
