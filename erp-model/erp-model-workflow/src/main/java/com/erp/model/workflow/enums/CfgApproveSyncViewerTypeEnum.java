package com.erp.model.workflow.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * ERP审批同步配置 可见范围dict_basic表viewerType 枚举
 * </p>
 *
 * @author jack
 * @since 2025-05-12 18:31:25
 */
public enum CfgApproveSyncViewerTypeEnum implements EnumMessage {
    TENANT("TENANT", "所有用户"),
    DEPARTMENT("DEPARTMENT", "指定部门"),
    USER("USER", "指定用户"),
	NONE("NONE", "不可见"),
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

    CfgApproveSyncViewerTypeEnum(String code, String name) {
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
        for (CfgApproveSyncViewerTypeEnum statusEnum : CfgApproveSyncViewerTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
