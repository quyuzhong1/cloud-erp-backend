package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MouldSettingTypeEnum implements EnumMessage {

    DOC("doc", "文档"), MOULD("mould", "模型");

    private final String code;

    private final String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
