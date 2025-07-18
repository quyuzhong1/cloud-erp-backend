package com.erp.model.workflow.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * ERP审批同步-通知配置 通知类型 枚举
 * </p>
 *
 * @author jack
 * @since 2025-05-12 18:28:31
 */
public enum CfgApproveNoticeNoticeTypeEnum implements EnumMessage {
	APPROVE("approve", "审批通知"),
	APPROVERESULT("approveResult", "审核结果通知"),
	CC("cc", "抄送通知"),
	TIMEOUTWARNING("timeoutWarning", "超时预警通知"),
	RECALL("recall", "撤回通知"),
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

    CfgApproveNoticeNoticeTypeEnum(String code, String name) {
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
        for (CfgApproveNoticeNoticeTypeEnum statusEnum : CfgApproveNoticeNoticeTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
