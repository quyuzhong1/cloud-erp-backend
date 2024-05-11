package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 开户银行类型
 */
public enum DeclareLabelTypeEnum {

    HIGH("general", "高申报"),
    LOW("aliPay", "低申报"),
    NORMAL("normal", "正常申报"),
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


    DeclareLabelTypeEnum(String code, String name) {
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
        for (DeclareLabelTypeEnum bankTypeEnum : DeclareLabelTypeEnum.values()) {
            if (code.equals(bankTypeEnum.getCode())) {
                return bankTypeEnum.getName();
            }
        }
        return "";
    }
}
