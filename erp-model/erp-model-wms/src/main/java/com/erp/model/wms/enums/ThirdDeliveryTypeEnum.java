package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ThirdDeliveryTypeEnum implements EnumMessage {
    ZY_TO_THIRD("zyToThird","自营仓发三方仓"),
    THIRD_TO_THIRD("thirdToThird","三方仓发三方仓"),
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
