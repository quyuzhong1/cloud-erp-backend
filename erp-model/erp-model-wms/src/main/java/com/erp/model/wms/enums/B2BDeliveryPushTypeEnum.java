package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * @author Lambda
 * @Classname DeliverTypeEnum
 * @Description 发货类型枚举
 * @Date 2023-12-29 10:30
 * @Created by yl
 */
@Getter
public enum B2BDeliveryPushTypeEnum implements EnumMessage {
    MANUAL("manual","手动发货"),
    API("api","API推送"),
    ;

    B2BDeliveryPushTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * 通过code查询
     * DeliverTypeEnum
     * 枚举
     */
    public static B2BDeliveryPushTypeEnum getByCode(String code) {
        return Stream.of(B2BDeliveryPushTypeEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
