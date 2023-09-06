package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum WaitInStockFlagEnum {
    WAIT_INSTOCK_QC("waitInStockQc", "待入库-已质检"),
    WAIT_INSTOCK_NOT_QC("waitInStockNotQc", "待入库-待质检"),
    ALL("all", "全部待入库");

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    WaitInStockFlagEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (WaitInStockFlagEnum item : WaitInStockFlagEnum.values()) {
                if (state.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static WaitInStockFlagEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getCode().equals(status))
                .findFirst().orElse(null);
    }
}
