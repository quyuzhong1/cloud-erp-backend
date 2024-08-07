package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PLM系统配置枚举
 */
@Getter
public enum PlmCfgSettingEnum implements EnumMessage{
    MATERIAL_ATTRIBUTE_CONTROL("materialAttributeControl", "金蝶物料属性控制"),
            ;
    private final String code;
    private final String name;
    PlmCfgSettingEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
