package com.erp.model.scm.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * '生效状态：notEffective=未生效,effective=生效中,expired=失效'
 * @author jack
 * @version 1.0
 * @date 2025-06-21
 */
public enum ContractInfoStatusEnum implements EnumMessage {

    NOT_EFFECTIVE("notEffective", "未生效"),
    EFFECTIVE("effective", "生效中"),
    EXPIRED("expired", "失效");

    private String code;
    private String name;

    ContractInfoStatusEnum(String code, String name) {
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
        for (ContractInfoStatusEnum statusEnum : ContractInfoStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

}
