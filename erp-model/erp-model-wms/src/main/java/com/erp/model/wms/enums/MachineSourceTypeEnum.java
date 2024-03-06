package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @author liuruipeng
 * @date 2024年03月06日 14:15
 */
@Getter
public enum MachineSourceTypeEnum implements EnumMessage {

    SO_INFO ("soInfo", "B2B销售订单"),
    FIRST_MILE_DELIVERY("firstMileDelivery", "FBA发货单"),
    SO_RETURN_INSTOCK("soReturnInstock", "退货入库单"),
    TRANSFER_APPLICATION("transferApplication", "调拨申请单"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    MachineSourceTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
