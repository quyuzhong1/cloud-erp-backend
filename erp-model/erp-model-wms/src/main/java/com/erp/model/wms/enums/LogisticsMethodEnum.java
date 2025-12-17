package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.rmi.ServerException;
import java.util.stream.Stream;

/**
 * 物流方式
 * @Author Luo_WG
 * @Date 2023/10/31 18:13
 **/
@Getter
@AllArgsConstructor
public enum LogisticsMethodEnum implements EnumMessage {
    LOCAL_DELIVERY("localDelivery", "本地发运"),
    AIRFREIGHT("airfreight", "空运"),
    EXPRESS("express", "快递"),
    OCEAN_FREIGHT_BULK("oceanFreightBulk", "海运散装"),
    OCEAN_FREIGHT_FCL("oceanFreightFCL", "海运整柜"),
    RAILWAY_TRANSPORTATION_BULK("railwayTransportationBulk", "铁运散装"),
    RAILWAY_TRANSPORTATION_FCL("railwayTransportationFCL", "铁运整柜"),

    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private final String code;
    /**
     * 名称
     */
    private final String name;


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

    /**
     * 通过code查询
     * LogisticsMethodEnum
     * 枚举
     */
    public static LogisticsMethodEnum getByCode(String code) {
        return Stream.of(LogisticsMethodEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 通过code查询
     * LogisticsMethodEnum
     * 枚举
     */
    public static LogisticsMethodEnum getByName(String name) {
        return Stream.of(LogisticsMethodEnum.values())
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * LogisticsMethodEnum
     * 枚举解析器
     */
    public static class LogisticsMethodDeserializer extends JsonDeserializer<LogisticsMethodEnum> {
        @Override
        public LogisticsMethodEnum deserialize(JsonParser p, DeserializationContext c) throws IOException {
            String value = p.getValueAsString();
            if (StringUtils.isBlank(value)) {
                return null;
            }
            LogisticsMethodEnum type = LogisticsMethodEnum.getByCode(value);
            if (type == null) {
                throw new ServerException("运输方式类型不存在:" + value);
            }
            return type;
        }
    }
}
