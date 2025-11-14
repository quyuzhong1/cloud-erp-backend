package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 样品台账搜索枚举
 */
public enum SampleLedgerTypeEnum implements EnumMessage {
    BORROW("borrow", "借用单"),
    BACK("back", "退回单"),
    SCRAP("scrap", "报废单"),
    EXHIBITION("exhibition", "展会订单"),
    TRANSFER("transfer", "转移单"),
    ;

    @EnumValue
    @JsonValue
    private String usage;
    private String name;

    SampleLedgerTypeEnum(String usage, String name) {
        this.usage = usage;
        this.name = name;
    }

    public String getUsage() {
        return usage;
    }

    @Override
    public String getCode() {
        return usage;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String usage) {
        if (StringUtils.isNotBlank(usage)) {
            for (SampleLedgerTypeEnum item : SampleLedgerTypeEnum.values()) {
                if (usage.equals(item.getUsage())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static SampleLedgerTypeEnum getByUsage(String usage) {
        return Arrays.stream(values()).filter(a -> a.getUsage().equals(usage))
                .findFirst().orElse(null);
    }

    public static List<String> getUsageList() {
        return Arrays.stream(SampleLedgerTypeEnum.values()).map(SampleLedgerTypeEnum::getUsage).collect(Collectors.toList());
    }

    public static List<String> getNameList() {
        return Arrays.stream(SampleLedgerTypeEnum.values()).map(SampleLedgerTypeEnum::getName).collect(Collectors.toList());
    }
}
