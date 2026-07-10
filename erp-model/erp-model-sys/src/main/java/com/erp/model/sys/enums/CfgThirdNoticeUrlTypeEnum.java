package com.erp.model.sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 三方通知配置 跳转链接类型 枚举
 * </p>
 *
 * @author jack
 * @since 2026-06-09
 */
public enum CfgThirdNoticeUrlTypeEnum implements EnumMessage {
    CUSTOM("custom", "自定义路径"),
    MENU("menu", "选择菜单"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    CfgThirdNoticeUrlTypeEnum(String code, String name) {
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
        for (CfgThirdNoticeUrlTypeEnum e : CfgThirdNoticeUrlTypeEnum.values()) {
            if (code.equals(e.getCode())) {
                return e.getName();
            }
        }
        return "";
    }
}
