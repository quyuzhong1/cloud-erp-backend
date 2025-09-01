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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 出库通知状态枚举
 * @author will
 * @date 2025/8/29 17:52
 */
@Getter
@AllArgsConstructor
public enum OutstockNoticeStatusEnum implements EnumMessage {
    WAIT_NOTICE("waitNotice","待通知出库"),
    PERMIT("permit","允许出库"),
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
        OutstockNoticeStatusEnum resultEnum = getByCode(code);
        return null == resultEnum ? "" : resultEnum.getName();
    }

    /**
     * 通过code查询
     * OverseasFinishStatus
     * 枚举
     */
    public static OutstockNoticeStatusEnum getByCode(String code) {
        return Stream.of(OutstockNoticeStatusEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * OverseasFinishStatus
     * 枚举解析器
     */
    public static class OverseasFinishStatusDeserializer extends JsonDeserializer<OutstockNoticeStatusEnum> {
        @Override
        public OutstockNoticeStatusEnum deserialize(JsonParser p, DeserializationContext c) throws IOException {
            String value = p.getValueAsString();
            if (StringUtils.isBlank(value)) {
                return null;
            }
            OutstockNoticeStatusEnum type = OutstockNoticeStatusEnum.getByCode(value);
            if (type == null) {
                throw new ServiceException("完结状态类型不存在:" + value);
            }
            return type;
        }
    }

    public static List<String> getStatusList() {
        return Arrays.stream(OutstockNoticeStatusEnum.values()).map(OutstockNoticeStatusEnum::getCode).collect(Collectors.toList());
    }
}
