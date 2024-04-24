package com.erp.model.oms.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lambda
 * @Classname PackageStatusEnum
 * @Description 中转状态状态
 * @Date 2024-01-19 10:34
 * @Created by yl
 */
public enum TransferStatusEnum implements EnumMessage {
    NOT("not","无需中转"),
    WAIT("wait","待中转"),
    SUCCESS("success","预报成功"),
    FAILURE("failure","预报失败"),
    ;

    /**
     * 类型
     */
    private String code;
    /**
     * 名称
     */
    private String name;

    TransferStatusEnum(String code, String name) {
        this.code=code;
        this.name=name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }


    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (TransferStatusEnum statusEnum : TransferStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
