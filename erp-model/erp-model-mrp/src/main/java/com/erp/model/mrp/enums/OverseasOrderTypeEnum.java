package com.erp.model.mrp.enums;

import cn.hutool.json.JSONArray;
import com.common.core.constant.EnumMessage;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum OverseasOrderTypeEnum implements EnumMessage {
    //海外仓发货
    OVERSEAS_WAREHOUSE("overseasWarehouse", "海外仓发货"),
    //本地自发货
    LOCAL_WAREHOUSE("localWarehouse", "本地自发货"),
    OVERSEAS_OTHER("overseasOther", "其他")
    ;


    private String code;
    private String name;

    OverseasOrderTypeEnum(String code, String name) {
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

    public static String getNameByCode(String code) {
        OverseasOrderTypeEnum[] stateEnums = values();
        for (OverseasOrderTypeEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }

    public static String getStringByCode(JSONArray orderType) {
        return Arrays.stream(OverseasOrderTypeEnum.values()).filter(v -> orderType.contains(v.getCode()))
                .map(OverseasOrderTypeEnum::getName)
                .collect(Collectors.joining(","));
    }
}
