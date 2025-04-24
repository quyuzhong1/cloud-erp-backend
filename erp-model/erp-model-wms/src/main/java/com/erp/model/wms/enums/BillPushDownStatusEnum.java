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
 * 【要货申请】
 * 拣货单下推状态
 * 发货单下推状态
 * @author jack
 * @since 2024-12-16
 */
@Getter
@AllArgsConstructor
public enum BillPushDownStatusEnum implements EnumMessage {
    NONE("none","无需下推"),
    WAIT("wait","未下推"),
    PART("part","部分下推"),
    FINISH("finish","已下推"),
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
        BillPushDownStatusEnum resultEnum = getByCode(code);
        return null == resultEnum ? "" : resultEnum.getName();
    }

    /**
     * 通过code查询
     * OverseasFinishStatus
     * 枚举
     */
    public static BillPushDownStatusEnum getByCode(String code) {
        return Stream.of(BillPushDownStatusEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * OverseasFinishStatus
     * 枚举解析器
     */
    public static class OverseasFinishStatusDeserializer extends JsonDeserializer<BillPushDownStatusEnum> {
        @Override
        public BillPushDownStatusEnum deserialize(JsonParser p, DeserializationContext c) throws IOException {
            String value = p.getValueAsString();
            if (StringUtils.isBlank(value)) {
                return null;
            }
            BillPushDownStatusEnum type = BillPushDownStatusEnum.getByCode(value);
            if (type == null) {
                throw new ServiceException("完结状态类型不存在:" + value);
            }
            return type;
        }
    }

    public static List<String> getStatusList() {
        return Arrays.stream(BillPushDownStatusEnum.values()).map(BillPushDownStatusEnum::getCode).collect(Collectors.toList());
    }
}
