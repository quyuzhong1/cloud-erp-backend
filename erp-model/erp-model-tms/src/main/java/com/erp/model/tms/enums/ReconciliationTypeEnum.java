package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zdy
 * @version 1.0
 * @description: 对账单类型
 * @date 2024/8/19 16:39
 */
public enum ReconciliationTypeEnum implements EnumMessage {

    ACTUAL("actual","实际账单"),
    INIT_PERIOD("initPeriod","期初账单")
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

    ReconciliationTypeEnum(String code, String name){
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }


    public static ReconciliationTypeEnum getEnumByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (ReconciliationTypeEnum typeEnums : ReconciliationTypeEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums;
            }
        }
        return null;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (ReconciliationTypeEnum typeEnums : ReconciliationTypeEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
