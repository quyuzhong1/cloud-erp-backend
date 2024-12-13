package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum LogisticsLargeShippingMethodEnum implements EnumMessage {
    AIRFREIGHT("airfreight", "空运"),
    EXPRESS("express", "快递"),
    OCEAN_FREIGHT_BULK("oceanFreightBulk", "海运散装"),
    OCEAN_FREIGHT_FCL("oceanFreightFCL", "海运整柜"),
    RAILWAY_TRANSPORTATION_BULK("railwayTransportationBulk", "铁运散装"),
    RAILWAY_TRANSPORTATION_FCL("railwayTransportationFCL", "铁运整柜"),
    EXPRESS_DELIVERY("expressDelivery","商业快递"),
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


    LogisticsLargeShippingMethodEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (LogisticsLargeShippingMethodEnum statusEnum : LogisticsLargeShippingMethodEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
