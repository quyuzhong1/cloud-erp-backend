package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: wtr
 * @Date: 2025/12/22 16:42
 * @Param:
 * @Return:
 * @Description:
 **/
public enum FbaOutStockTypeEnum implements EnumMessage {

    AWD(1,"awd"),
    FBA(0,"fba"),
    ;

    @EnumValue
    @JsonValue
    private int code;
    private String name;

   FbaOutStockTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Object getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (FbaOutStockTypeEnum item : FbaOutStockTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
