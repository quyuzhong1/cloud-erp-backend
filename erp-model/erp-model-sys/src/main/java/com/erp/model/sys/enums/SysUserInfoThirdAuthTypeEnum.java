package com.erp.model.sys.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 *
 * </p>
 *
 * @author jack
 * @since 2026-01-08
 */
public enum SysUserInfoThirdAuthTypeEnum implements EnumMessage {
	FS("FS", "飞书"),
//	DD("dd", "钉钉"),
//	QW("qw", "企业微信"),
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

    SysUserInfoThirdAuthTypeEnum(String code, String name) {
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
        for (SysUserInfoThirdAuthTypeEnum statusEnum : SysUserInfoThirdAuthTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
