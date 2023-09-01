package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lambda
 * @Classname TypeEnum
 * @Description TODO
 * @Date 2023-08-18 11:00
 * @Created by yl
 */
public enum TypeEnum  implements EnumMessage {
    PLATFORM("platform","平台"),
    WAREHOUSE("warehouse","仓库"),
    ASSIGN("assign","指定物流"),
    MIN_FREIGHT("minFreight","最低运费")
    ;

    TypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public String code;
    /**
     * 名称
     */
    private String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (TypeEnum typeEnum : TypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }
}
