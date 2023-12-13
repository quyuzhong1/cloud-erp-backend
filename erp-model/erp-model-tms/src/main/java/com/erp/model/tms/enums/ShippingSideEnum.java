package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: 边长枚举
 * @date 2023/11/10 11:35
 */
public enum ShippingSideEnum implements EnumMessage {

    LONGEST_EDGE("longestEdge","最长边"),
    MINOR_EDGE("minorEdge","次长边"),
    EDGEL_SUM("edgelSum","三边和"),
    ANY_EDGE("anyEdge","任意一边")
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

    ShippingSideEnum(String code, String name){
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

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (ShippingSideEnum typeEnums : ShippingSideEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
