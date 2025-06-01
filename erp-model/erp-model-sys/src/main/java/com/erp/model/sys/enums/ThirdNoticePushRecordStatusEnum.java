package com.erp.model.sys.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 三方通知推送记录 状态 枚举
 * </p>
 *
 * @author jack
 * @since 2025-05-26 17:54:21
 */
public enum ThirdNoticePushRecordStatusEnum implements EnumMessage {
	SUCCESS("success", "推送成功"),
	FAILED("failed", "推送失败"),
	SENDING("sending", "推送中"),
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

    ThirdNoticePushRecordStatusEnum(String code, String name) {
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
        for (ThirdNoticePushRecordStatusEnum statusEnum : ThirdNoticePushRecordStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
