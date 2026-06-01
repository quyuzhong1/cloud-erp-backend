package com.erp.model.oms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 寄样费用尾程费用来源
 */
@Getter
@AllArgsConstructor
public enum KolSampleCostFeeSourceEnum implements EnumMessage {

    SMALL_BAG_ALLOCATION("smallBagAllocation", "尾程费用分摊"),
    IMPORT("import", "尾程费用导入"),
    ;

    private final String code;

    private final String name;
}
