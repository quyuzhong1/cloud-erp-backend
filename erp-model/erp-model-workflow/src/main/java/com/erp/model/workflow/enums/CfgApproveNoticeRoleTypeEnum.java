package com.erp.model.workflow.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * ERP审批同步-通知配置 角色 枚举
 * </p>
 *
 * @author jack
 * @since 2025-05-12 18:28:31
 */
public enum CfgApproveNoticeRoleTypeEnum implements EnumMessage {
	APPLICANT("applicant", "申请人"),
	CC("cc", "抄送人"),
	APPROVER("approver", "审核人"),
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

    CfgApproveNoticeRoleTypeEnum(String code, String name) {
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
        for (CfgApproveNoticeRoleTypeEnum statusEnum : CfgApproveNoticeRoleTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
