package com.erp.model.oms.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lambda
 * @Classname PackageStatusEnum
 * @Description 组包状态
 * @Date 2024-01-19 10:34
 * @Created by yl
 */
public enum PackageStatusEnum implements EnumMessage {
    NOT("not","不需要"),
    WAIT("wait","待组包"),
    ALREADY("already","已组包"),
    ;

    /**
     * 类型
     */
    private String code;
    /**
     * 名称
     */
    private String name;

    PackageStatusEnum(String code, String name) {
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
        for (PackageStatusEnum statusEnum : PackageStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
