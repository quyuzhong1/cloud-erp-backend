package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @description: 系统配置委外发料单生成类型枚举
 * @author Will
 * @date: 2024/1/19 9:37
 */
public enum CfgSettingCreateTypeEnum implements EnumMessage {
    AUTO_CREATE_SUBMIT("autoCreateSubmit", "自动生成【待提交】"),
    AUTO_CREATE_APPROVE("autoCreateApprove", "自动生成【已审核】"),
    NOT_AUTO_CREATE("notAutoCreate", "不自动生成"),
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

    CfgSettingCreateTypeEnum(String code, String name) {
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
        if (StringUtils.isNotBlank(code)) {
            for (CfgSettingCreateTypeEnum item : CfgSettingCreateTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

}
