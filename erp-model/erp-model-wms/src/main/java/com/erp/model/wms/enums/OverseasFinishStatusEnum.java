package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
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
 * 完结状态
 *
 * @author Jim
 * @since 2023-11-16
 */
@Getter
@AllArgsConstructor
public enum OverseasFinishStatusEnum implements EnumMessage {
    NOT("not","未完结"),
    AUTO("auto","自动完结"),
    MANUAL("manual","手动完结"),
    ;

    @EnumValue
    private final String code;
    private final String name;

    /**
     * 通过code查询
     * OverseasFinishStatus
     * 枚举名称
     */
    public static String getNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        OverseasFinishStatusEnum resultEnum = getByCode(code);
        return null == resultEnum ? "" : resultEnum.getName();
    }

    /**
     * 通过code查询
     * OverseasFinishStatus
     * 枚举
     */
    public static OverseasFinishStatusEnum getByCode(String code) {
        return Stream.of(OverseasFinishStatusEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * OverseasFinishStatus
     * 枚举解析器
     */
    public static class OverseasFinishStatusDeserializer extends JsonDeserializer<OverseasFinishStatusEnum> {
        @Override
        public OverseasFinishStatusEnum deserialize(JsonParser p, DeserializationContext c) throws IOException {
            String value = p.getValueAsString();
            if (StringUtils.isBlank(value)) {
                return null;
            }
            OverseasFinishStatusEnum type = OverseasFinishStatusEnum.getByCode(value);
            if (type == null) {
                throw new ServerException("完结状态类型不存在:" + value);
            }
            return type;
        }
    }
}
