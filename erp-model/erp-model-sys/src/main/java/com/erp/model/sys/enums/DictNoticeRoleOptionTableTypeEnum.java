package com.erp.model.sys.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 *  表类型 枚举
 * </p>
 *
 * @author jack
 * @since 2025-06-04 17:06:58
 */
public enum DictNoticeRoleOptionTableTypeEnum implements EnumMessage {
	TABLE("table", "主表"),
	DETAIL("detail", "明细表"),
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

    DictNoticeRoleOptionTableTypeEnum(String code, String name) {
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
        for (DictNoticeRoleOptionTableTypeEnum statusEnum : DictNoticeRoleOptionTableTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
