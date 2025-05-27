package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 签收来源类型枚举
 */
public enum SignSourceTypeEnum implements EnumMessage {
    API("api", "API同步"),
    MANUAL("manual", "手动签收"),
    CHANGE("change", "调整签收"),
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



    SignSourceTypeEnum(String code, String name) {
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

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SignSourceTypeEnum billTypeEnum : SignSourceTypeEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }


    public static List<SignSourceTypeEnum> listByCode(String code) {
        List<SignSourceTypeEnum> list = new ArrayList<>();
        for (SignSourceTypeEnum billTypeEnum : SignSourceTypeEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                list.add(billTypeEnum);
            }
        }
        return list;
    }
}
