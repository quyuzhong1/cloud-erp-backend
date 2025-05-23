package com.erp.model.workflow.enums;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/23 21:04
 */

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 *@Author: hcg
 *@CreateTime: 2025-05-23
 *@Description:
 *@Version: 1.0
 */
public enum CfgThirdProcessSourcePlatformEnum implements EnumMessage {
    FS_AUDIT("fsAudit", "飞书审核"),
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

    CfgThirdProcessSourcePlatformEnum(String code, String name) {
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
        for (CfgQueryOptionFieldTypeEnum statusEnum : CfgQueryOptionFieldTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
