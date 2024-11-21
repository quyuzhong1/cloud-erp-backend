package com.erp.model.oms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 客户表 平台类型 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-11-14 11:15:54
 */
public enum CustomerInfoBusinessModeEnum implements EnumMessage {
	O2B("o2b", "线上2B"),
	O2C("o2c", "线上2C"),
	X2B("x2b", "线下2B"),
	X2C("x2c", "线上2C"),
	O2O("o2o", "O2O"),
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

    CustomerInfoBusinessModeEnum(String code, String name) {
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
        for (CustomerInfoBusinessModeEnum statusEnum : CustomerInfoBusinessModeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
