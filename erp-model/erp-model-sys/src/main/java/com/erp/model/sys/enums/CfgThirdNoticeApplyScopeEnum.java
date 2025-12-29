package com.erp.model.sys.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 三方通知配置 适用范围 枚举
 * </p>
 *
 * @author jack
 * @since 2025-12-26 10:35:11
 */
public enum CfgThirdNoticeApplyScopeEnum implements EnumMessage {
	BILL_TYPE("bill_type", "单据类型"),
	BILL_HEADER("bill_header", "单据整单"),
	BILL_LINE("bill_line", "单据明细"),
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

    CfgThirdNoticeApplyScopeEnum(String code, String name) {
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
        for (CfgThirdNoticeApplyScopeEnum statusEnum : CfgThirdNoticeApplyScopeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
