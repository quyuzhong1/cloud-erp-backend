package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Getter
public enum ThirdDeliveryTypeEnum implements EnumMessage {
    SELF_TO_THIRD("selfToThird","自营仓发三方仓"),
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

    ThirdDeliveryTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (ThirdDeliveryTypeEnum item : ThirdDeliveryTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static ThirdDeliveryTypeEnum getByCode(String code) {
        return Arrays.stream(ThirdDeliveryTypeEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

}
