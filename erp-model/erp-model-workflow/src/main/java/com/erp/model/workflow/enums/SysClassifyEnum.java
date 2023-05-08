package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.List;

public enum SysClassifyEnum {
    PLM("plm", "PLM系统"),
    SCM("scm", "SCM系统"),
    WMS("wms", "WMS系统");

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    SysClassifyEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
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
        List<SysClassifyEnum> resultList = new ArrayList<>();
        for (SysClassifyEnum optionEnum : SysClassifyEnum.values()) {
            resultList.add(optionEnum);
        }
        return resultList;
    }
}
