package com.erp.model.oms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 客户表 交易模式 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-11-14 11:15:54
 */
public enum CustomerInfoTransactionalModeEnum implements EnumMessage {
	DBJY("dbjy", "担保交易"),
	XKHH("xkhh", "先款后货"),
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

    CustomerInfoTransactionalModeEnum(String code, String name) {
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
        for (CustomerInfoTransactionalModeEnum statusEnum : CustomerInfoTransactionalModeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
