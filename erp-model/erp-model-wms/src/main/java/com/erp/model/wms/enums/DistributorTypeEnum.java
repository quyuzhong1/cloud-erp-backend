package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum DistributorTypeEnum implements EnumMessage {
    GOOD_CANG_LOGISTICS(0, "谷仓合作物流"),
    GOOD_CANG_TRANSPORT_LOGISTICS(1, "客户物流（谷仓交运）"),
    SELF_LOGISTICS(2, "客户物流（自提）"),
    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private Integer code;
    /**
     * 名称
     */
    private String name;

    public static String getName(Integer code) {
        if (Objects.nonNull(code)) {
            for (DistributorTypeEnum item : DistributorTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static DistributorTypeEnum getByCode(Integer code) {
        DistributorTypeEnum[] eumnList = DistributorTypeEnum.values();
        for (DistributorTypeEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
