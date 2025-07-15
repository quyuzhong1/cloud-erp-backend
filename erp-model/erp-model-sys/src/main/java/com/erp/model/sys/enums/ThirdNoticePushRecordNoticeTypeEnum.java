package com.erp.model.sys.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 三方通知推送记录 通知类型 枚举
 * </p>
 *
 * @author jack
 * @since 2025-05-26 17:54:21
 */
public enum ThirdNoticePushRecordNoticeTypeEnum implements EnumMessage {
	MESSAGEPUSH("messagePush", "消息通知"),
	APPROVALPUSH("approvalPush", "审批推送"),
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

    ThirdNoticePushRecordNoticeTypeEnum(String code, String name) {
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
        for (ThirdNoticePushRecordNoticeTypeEnum statusEnum : ThirdNoticePushRecordNoticeTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
