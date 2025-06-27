package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * @description:对账周期生成类型
 * @author Will
 * @date: 2024/1/12 11:46
 */
public enum ReconciliationTypeEnum implements EnumMessage {


    CREAT_BY_PERIOD("creatByPeriod", "按周期生成"),
    CREAT_BY_MONTH("creatByMonth", "自然月"),

    ;

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

    ReconciliationTypeEnum(String code, String name) {
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

    public static ReconciliationTypeEnum getByCode(String code) {
        return Arrays.stream(ReconciliationTypeEnum.values())
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
    public static String getNameByCode(String code) {
        for (ReconciliationTypeEnum e : ReconciliationTypeEnum.values()) {
            if (e.getCode().equals(code)) {
                return e.getName();
            }
        }
        return "";
    }
}
