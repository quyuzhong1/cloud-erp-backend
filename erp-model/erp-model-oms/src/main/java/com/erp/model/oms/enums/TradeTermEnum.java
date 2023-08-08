package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum TradeTermEnum implements EnumMessage {
    EXW("EXW", "EXW"),
    FOB("FOB", "FOB"),
    FCA("FCA", "FCA"),
    DDP("DAP", "DDP"),
    DAP("DDU", "DAP"),
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


    TradeTermEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (TradeTermEnum tradeTermEnum : TradeTermEnum.values()) {
            if (code.equals(tradeTermEnum.getCode())) {
                return tradeTermEnum.getName();
            }
        }
        return "";
    }
}
