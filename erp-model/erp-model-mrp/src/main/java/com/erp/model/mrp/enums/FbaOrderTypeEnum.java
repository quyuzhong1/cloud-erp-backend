package com.erp.model.mrp.enums;

import cn.hutool.json.JSONArray;
import com.common.core.constant.EnumMessage;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum FbaOrderTypeEnum implements EnumMessage {
    //fba
    FBA("fba", "FBA"),
    //fbm
    FBM("fbm", "FBM");


    private String code;
    private String name;

    FbaOrderTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getStringByCode(JSONArray orderType) {
        return Arrays.stream(FbaOrderTypeEnum.values()).filter(v -> orderType.contains(v.getCode()))
                .map(FbaOrderTypeEnum::getName)
                .collect(Collectors.joining(","));
    }


    @Override
    public String getCode() {
        return code;
    }
    @Override
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        FbaOrderTypeEnum[] stateEnums = values();
        for (FbaOrderTypeEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
}
