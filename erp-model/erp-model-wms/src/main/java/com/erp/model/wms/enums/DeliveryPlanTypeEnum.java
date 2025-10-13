package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeliveryPlanTypeEnum implements EnumMessage {
    FBA("fba","FBA发货计划"),
    THIRD_WAREHOUSE("thirdWarehouse","第三方仓发货计划"),
    ALIEXPRESS("AliExpress","速卖通发货计划"),
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
