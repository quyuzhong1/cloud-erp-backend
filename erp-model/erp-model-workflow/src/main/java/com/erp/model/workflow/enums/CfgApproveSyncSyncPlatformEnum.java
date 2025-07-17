package com.erp.model.workflow.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * ERP审批同步配置 同步平台 枚举
 * </p>
 *
 * @author jack
 * @since 2025-05-12 18:31:25
 */
public enum CfgApproveSyncSyncPlatformEnum implements EnumMessage {
	FEISHU("feishu", "飞书"),
	DD("dd", "钉钉"),
	QW("qw", "企业微信"),
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

    CfgApproveSyncSyncPlatformEnum(String code, String name) {
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
        for (CfgApproveSyncSyncPlatformEnum statusEnum : CfgApproveSyncSyncPlatformEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
