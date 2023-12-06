package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.enums.GoodCangEnums;
import com.common.core.constant.EnumMessage;
import com.common.core.exception.ServiceException;
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
 * 【海外仓入库单】
 * 交货方式类型
 *
 * @author Jim
 * @since 2023-11-16
 */
@Getter
@AllArgsConstructor
public enum OverseasDeliveryModeEnum implements EnumMessage {
    SELF_DELIVERY("selfDelivery", "自送货物", GoodCangEnums.OpenCollectingServiceEnum.SELF_DELIVERED_GOODS),
    COLLECT_AT_HOME("collectAtHome", "上门揽收", GoodCangEnums.OpenCollectingServiceEnum.PICK_UP),
    ;

    @EnumValue
    private final String code;
    private final String name;
    private final GoodCangEnums.OpenCollectingServiceEnum serviceEnum;

    /**
     * 通过code查询
     * OverseasDeliveryModeEnum
     * 枚举名称
     */
    public static String getNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        OverseasDeliveryModeEnum resultEnum = getByCode(code);
        return null == resultEnum ? "" : resultEnum.getName();
    }

    /**
     * 通过code查询
     * OverseasDeliveryModeEnum
     * 枚举
     */
    public static OverseasDeliveryModeEnum getByCode(String code) {
        return Stream.of(OverseasDeliveryModeEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * OverseasDeliveryModeEnum
     * 枚举解析器
     */
    public static class OverseasDeliveryModeDeserializer extends JsonDeserializer<OverseasDeliveryModeEnum> {
        @Override
        public OverseasDeliveryModeEnum deserialize(JsonParser p, DeserializationContext c) throws IOException {
            String value = p.getValueAsString();
            if (StringUtils.isBlank(value)) {
                return null;
            }
            OverseasDeliveryModeEnum type = OverseasDeliveryModeEnum.getByCode(value);
            if (type == null) {
                throw new ServiceException("交货方式类型不存在:" + value);
            }
            return type;
        }
    }
}
