package com.erp.model.workflow.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * ERP审批同步-通知配置 状态 枚举
 * </p>
 *
 * @author jack
 * @since 2025-05-12 18:46:57
 */
public enum ApproveSyncRecordStatusEnum implements EnumMessage {
	SUCCESS("success", "推送成功"),
	FAILED("failed", "推送失败"),
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

    ApproveSyncRecordStatusEnum(String code, String name) {
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
        for (ApproveSyncRecordStatusEnum statusEnum : ApproveSyncRecordStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
