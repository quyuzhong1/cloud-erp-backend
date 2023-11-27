package com.common.business.enums;

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
 * 【请求ID类型】
 *
 * @author Jim
 * @since 2023-11-27
 */
@Getter
@AllArgsConstructor
public enum RequestIdTypeEnum implements EnumMessage {
    MAIN_ID("main_id","单据ID"),
    DETAIL_ID("detail_id","详情ID"),
    ;

    @EnumValue
    private final String code;
    private final String name;

    /**
     * 通过code查询
     * RequestIdEnum
     * 枚举名称
     */
    public static String getNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        RequestIdTypeEnum resultEnum = getByCode(code);
        return null == resultEnum ? "" : resultEnum.getName();
    }

    /**
     * 通过code查询
     * RequestIdEnum
     * 枚举
     */
    public static RequestIdTypeEnum getByCode(String code) {
        return Stream.of(RequestIdTypeEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * RequestIdEnum
     * 枚举解析器
     */
    public static class RequestIdEnumDeserializer extends JsonDeserializer<RequestIdTypeEnum> {
        @Override
        public RequestIdTypeEnum deserialize(JsonParser p, DeserializationContext c) throws IOException {
            String value = p.getValueAsString();
            if (StringUtils.isBlank(value)) {
                return null;
            }
            RequestIdTypeEnum type = RequestIdTypeEnum.getByCode(value);
            if (type == null) {
                throw new ServiceException("请求ID类型不存在:" + value);
            }
            return type;
        }
    }
}
