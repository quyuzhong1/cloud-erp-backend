package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @description: 系统配置委外入库配置
 * @author zdy
 * @date: 2024/1/19 9:37
 */
public enum CfgSettingSubcontractTypeEnum implements EnumMessage {
    //manual 不自动 auto 自动  semiAuto 部分自动
    MANUAL("manual", "不自动"),
    AUTO("auto", "自动"),
    SEMI_AUTO("semiAuto", "部分自动"),
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

    CfgSettingSubcontractTypeEnum(String code, String name) {
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
            for (CfgSettingSubcontractTypeEnum item : CfgSettingSubcontractTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

}
