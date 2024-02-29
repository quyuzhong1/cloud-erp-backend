package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.common.core.exception.ServiceException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * 【sku映射】
 * 发货类型
 *
 * @author Jim
 * @since 2023-02-27
 */
@Getter
@AllArgsConstructor
public enum WarehouseDeliveryTypeEnum implements EnumMessage {
    COMBINE("combine", "捆绑SKU发货"),
    SINGLE("single", "子件SKU发货"),
    ;

    @EnumValue
    private final String code;
    private final String name;


    /**
     * 通过code查询
     * SkuDeliveryTypeEnum
     * 枚举名称
     */
    public static String getNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        WarehouseDeliveryTypeEnum resultEnum = getByCode(code);
        return null == resultEnum ? "" : resultEnum.getName();
    }

    /**
     * 通过code查询
     * SkuDeliveryTypeEnum
     * 枚举
     */
    public static WarehouseDeliveryTypeEnum getByCode(String code) {
        return Stream.of(WarehouseDeliveryTypeEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * SkuDeliveryTypeEnum
     * 枚举解析器
     */
    public static class SkuDeliveryTypeEnumDeserializer extends JsonDeserializer<WarehouseDeliveryTypeEnum> {
        @Override
        public WarehouseDeliveryTypeEnum deserialize(JsonParser p, DeserializationContext c) throws IOException {
            String value = p.getValueAsString();
            if (StringUtils.isBlank(value)) {
                return null;
            }
            WarehouseDeliveryTypeEnum type = WarehouseDeliveryTypeEnum.getByCode(value);
            if (type == null) {
                throw new ServiceException("交货方式类型不存在:" + value);
            }
            return type;
        }
    }
}
