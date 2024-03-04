package com.erp.model.oms.enums;

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
 * 【店铺在平台的状态】
 *
 * @author Jim
 * @since 2023-11-16
 */
@Getter
@AllArgsConstructor
public enum ShopPlatformStatusEnum implements EnumMessage {
    NONE("none", "无"),
    OPEN("open", "正常"),
    CLOSED("closed", "关闭"),


    ;

    @EnumValue
    private final String code;
    private final String name;

    /**
     * 通过code查询
     * ShopPlatformStatusEnum
     * 枚举名称
     */
    public static String getNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        ShopPlatformStatusEnum resultEnum = getByCode(code);
        return null == resultEnum ? "" : resultEnum.getName();
    }

    /**
     * 通过code查询
     * ShopPlatformStatusEnum
     * 枚举
     */
    public static ShopPlatformStatusEnum getByCode(String code) {
        return Stream.of(ShopPlatformStatusEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * ShopPlatformStatusEnum
     * 枚举解析器
     */
    public static class ShopPlatformStatusEnumsDeserializer extends JsonDeserializer<ShopPlatformStatusEnum> {
        @Override
        public ShopPlatformStatusEnum deserialize(JsonParser p, DeserializationContext c) throws IOException {
            String value = p.getValueAsString();
            if (StringUtils.isBlank(value)) {
                return null;
            }
            ShopPlatformStatusEnum type = ShopPlatformStatusEnum.getByCode(value);
            if (type == null) {
                throw new ServiceException("类型不存在:" + value);
            }
            return type;
        }
    }
}
