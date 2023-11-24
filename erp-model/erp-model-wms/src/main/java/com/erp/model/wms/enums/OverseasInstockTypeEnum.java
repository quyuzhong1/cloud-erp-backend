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
 * 入库类型
 *
 * @author Jim
 * @since 2023-11-21
 */
@Getter
@AllArgsConstructor
public enum OverseasInstockTypeEnum implements EnumMessage {
    SELF_HEADWAY("selfHeadway", "自发头程"),
    TRANSFER_AGENT("transferAgent", "中转代发"),
    ;

    @EnumValue
    private final String code;
    private final String name;

    /**
     * 通过code查询
     * OverseasInstockTypeEnum
     * 枚举名称
     */
    public static String getNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        OverseasInstockTypeEnum resultEnum = getByCode(code);
        return null == resultEnum ? "" : resultEnum.getName();
    }

    /**
     * 通过code查询
     * OverseasInstockTypeEnum
     * 枚举
     */
    public static OverseasInstockTypeEnum getByCode(String code) {
        return Stream.of(OverseasInstockTypeEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * OverseasInstockType
     * 枚举解析器
     */
    public static class OverseasInStockTypeDeserializer extends JsonDeserializer<OverseasInstockTypeEnum> {
        @Override
        public OverseasInstockTypeEnum deserialize(JsonParser p, DeserializationContext c) throws IOException {
            String value = p.getValueAsString();
            if (StringUtils.isBlank(value)) {
                return null;
            }
            OverseasInstockTypeEnum type = OverseasInstockTypeEnum.getByCode(value);
            if (type == null) {
                throw new ServerException("入库类型不存在:" + value);
            }
            return type;
        }
    }
}
