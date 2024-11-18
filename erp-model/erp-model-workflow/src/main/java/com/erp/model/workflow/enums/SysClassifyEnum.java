package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum SysClassifyEnum {
    PLM("plm", "PLM系统"),
    SCM("scm", "SCM系统"),
    WMS("wms", "WMS系统"),
    OMS("oms", "OMS系统"),
    FM("fm", "头程系统"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    SysClassifyEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(String code) {
        for (SysClassifyEnum state : SysClassifyEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static SysClassifyEnum getEnumByCode(String code) {
        for (SysClassifyEnum state : SysClassifyEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }

    public static List<SysClassifyEnum> getAll() {
        return Arrays.stream(SysClassifyEnum.values()).collect(Collectors.toList());
    }
}
