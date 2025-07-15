package com.erp.model.sys.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 三方通知配置 通知方式 枚举
 * </p>
 *
 * @author jack
 * @since 2025-05-23 09:29:51
 */
public enum CfgThirdNoticeMethodEnum implements EnumMessage {
	SINGLE("single", "单条"),
	SUMMARY("summary", "汇总"),
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

    CfgThirdNoticeMethodEnum(String code, String name) {
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
        for (CfgThirdNoticeMethodEnum statusEnum : CfgThirdNoticeMethodEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
