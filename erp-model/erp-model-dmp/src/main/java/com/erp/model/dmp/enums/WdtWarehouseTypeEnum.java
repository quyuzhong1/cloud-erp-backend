package com.erp.model.dmp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WdtWarehouseTypeEnum implements EnumMessage {
    NORMAL("1", "普通（内部）"),
    SELF_TRANSFER("2", "自流转"),
    PLATFORM("3", "平台"),
    JD("4", "京东沧海"),
    TIKTOK("6", "抖音云仓"),
    CONSIGNMENT("125", "代发仓"),
    DISTRIBUTION("126", "分销委外");

    private final String code;

    private final String name;
}
