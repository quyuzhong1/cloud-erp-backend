package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutStockModeEnum implements EnumMessage {
    FIFO("FIFO", "先进先出"),
    QUANTITY_FIRST("QUANTITY_FIRST", "数量优先");
    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

}
