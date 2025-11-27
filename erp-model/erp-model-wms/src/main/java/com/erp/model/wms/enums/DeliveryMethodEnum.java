package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Getter
public enum DeliveryMethodEnum implements EnumMessage {
    EXPRESS("EXPRESS","渠道订单"),
    SELF("SELF","自提订单"),
    TRUCK("TRUCK","卡车订单"),
    TRUCK_SELF("TRUCK_SELF","卡车自提"),
    ;

    /**
     * 类型
     */
    private final String code;
    /**
     * 名称
     */
    private final String name;

    DeliveryMethodEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (DeliveryMethodEnum item : DeliveryMethodEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static DeliveryMethodEnum getByCode(String code) {
        return Arrays.stream(DeliveryMethodEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

}
