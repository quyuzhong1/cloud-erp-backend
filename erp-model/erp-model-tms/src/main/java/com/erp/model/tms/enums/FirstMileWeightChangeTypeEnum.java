package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * 头程重量分摊修改类型
 */
public enum FirstMileWeightChangeTypeEnum implements EnumMessage {

    CHANGE_PRODUCT_WEIGHT("changeProductWeight", "修改单查询重量"),
    CHANGE_OUTSTOCK_SIZE("changeOutstockSize", "修改出库重量/尺寸"),
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


    FirstMileWeightChangeTypeEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (FirstMileWeightChangeTypeEnum statusEnum : FirstMileWeightChangeTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static FirstMileWeightChangeTypeEnum getByCode(String code) {
        return Arrays.stream(FirstMileWeightChangeTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }
}


