package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CalcStatusEnum implements EnumMessage {

    DOING("doing", "进行中"),
    FINISH("finish", "已完成");

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
