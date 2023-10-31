package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流方式
 * @Author Luo_WG
 * @Date 2023/10/31 18:13
 **/
public enum LogisticsMethodEnum implements EnumMessage {
    AIRFREIGHT("airfreight", "空运"),
    EXPRESS("express", "快递"),
    OCEAN_FREIGHT_BULK("oceanFreightBulk", "海运散装"),
    OCEAN_FREIGHT_FCL("oceanFreightFCL", "海运整箱"),
    RAILWAY_TRANSPORTATION_BULK("railwayTransportationBulk", "铁运散装"),
    RAILWAY_TRANSPORTATION_FCL("railwayTransportationFCL", "铁运整箱"),
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

    LogisticsMethodEnum(String code, String name) {
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
        if (StringUtils.isNotBlank(code)) {
            for (LogisticsMethodEnum item : LogisticsMethodEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
