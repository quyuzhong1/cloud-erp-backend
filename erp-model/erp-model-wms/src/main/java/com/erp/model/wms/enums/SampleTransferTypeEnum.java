package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 样品转移单转移类型枚举
 */
public enum SampleTransferTypeEnum implements EnumMessage {
    INTERNAL_TRANSFER("internalTransfer", "公司内部转移"),
    EXTERNAL_TRANSFER("externalTransfer", "公司外部转移")
    ;

    @EnumValue
    @JsonValue
    private String transferType;
    private String name;

    SampleTransferTypeEnum(String transferType, String name) {
        this.transferType = transferType;
        this.name = name;
    }

    public String getTransferType() {
        return transferType;
    }

    @Override
    public String getCode() {
        return transferType;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String transferType) {
        if (StringUtils.isNotBlank(transferType)) {
            for (SampleTransferTypeEnum item : SampleTransferTypeEnum.values()) {
                if (transferType.equals(item.getTransferType())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static String getTransferTypeByName(String name) {
        if (StringUtils.isNotBlank(name)) {
            for (SampleTransferTypeEnum item : SampleTransferTypeEnum.values()) {
                if (name.equals(item.getName())) {
                    return item.getTransferType();
                }
            }
        }
        return "";
    }

    public static SampleTransferTypeEnum getByTransferType(String transferType) {
        return Arrays.stream(values()).filter(a -> a.getTransferType().equals(transferType))
                .findFirst().orElse(null);
    }

    public static List<String> getTransferTypeList() {
        return Arrays.stream(SampleTransferTypeEnum.values()).map(SampleTransferTypeEnum::getTransferType).collect(Collectors.toList());
    }

    public static List<String> getNameList() {
        return Arrays.stream(SampleTransferTypeEnum.values()).map(SampleTransferTypeEnum::getName).collect(Collectors.toList());
    }
}
