package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 样品使用方式枚举
 */
public enum SampleUsageScopeEnum implements EnumMessage {
    INTERNAL_USE("internalUse", "公司内部使用"),
    EXTERNAL_USE("externalUse", "公司外部使用")
    ;

    @EnumValue
    @JsonValue
    private String usageScope;
    private String name;

    SampleUsageScopeEnum(String usageScope, String name) {
        this.usageScope = usageScope;
        this.name = name;
    }

    public String getUsageScope() {
        return usageScope;
    }

    @Override
    public String getCode() {
        return usageScope;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String usageScope) {
        if (StringUtils.isNotBlank(usageScope)) {
            for (SampleUsageScopeEnum item : SampleUsageScopeEnum.values()) {
                if (usageScope.equals(item.getUsageScope())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static String getUsageScopeByName(String name) {
        if (StringUtils.isNotBlank(name)) {
            for (SampleUsageScopeEnum item : SampleUsageScopeEnum.values()) {
                if (name.equals(item.getName())) {
                    return item.getUsageScope();
                }
            }
        }
        return "";
    }

    public static SampleUsageScopeEnum getByUsageScope(String usageScope) {
        return Arrays.stream(values()).filter(a -> a.getUsageScope().equals(usageScope))
                .findFirst().orElse(null);
    }

    public static List<String> getUsageScopeList() {
        return Arrays.stream(SampleUsageScopeEnum.values()).map(SampleUsageScopeEnum::getUsageScope).collect(Collectors.toList());
    }

    public static List<String> getNameList() {
        return Arrays.stream(SampleUsageScopeEnum.values()).map(SampleUsageScopeEnum::getName).collect(Collectors.toList());
    }
}
