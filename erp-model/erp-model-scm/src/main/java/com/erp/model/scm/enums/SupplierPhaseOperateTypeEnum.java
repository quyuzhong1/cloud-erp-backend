package com.erp.model.scm.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SupplierPhaseOperateTypeEnum implements EnumMessage {
    UPGRADE("upgrade", "升级"),
    DEGRADE("degrade", "降级"),
    ;


    /**
     * 类型
     */
    private final String code;
    /**
     * 名称
     */
    private final String name;

}
