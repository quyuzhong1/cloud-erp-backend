package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 仓位补货类型 枚举
 * @date 2024-06-26
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum ReplenishTypeEnum implements EnumMessage {
    DELIVER_STOCK_OUT("deliver_stock_out", "发货缺货补货"),
    SAFETY_INVENTORY("safety_inventory", "安全库存补货")
    ;

    private String code;
    private String name;
}
