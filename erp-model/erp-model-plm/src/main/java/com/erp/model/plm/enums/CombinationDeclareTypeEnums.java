package com.erp.model.plm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lambda
 * @Classname CombinationDeclareTypeEnums
 * @Description TODO
 * @Date 2023-11-10 10:12
 * @Created by yl
 */
public enum CombinationDeclareTypeEnums implements EnumMessage {
    SPLIT("split", "拆分申报"),
    COMBINE("combine", "组合申报"),
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

    CombinationDeclareTypeEnums(String code, String name) {
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
        for (CombinationDeclareTypeEnums typeEnums : CombinationDeclareTypeEnums.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }


    public static String getCode(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (CombinationDeclareTypeEnums typeEnums : CombinationDeclareTypeEnums.values()) {
            if (name.equals(typeEnums.getName())) {
                return typeEnums.getCode();
            }
        }
        return "";
    }

}
