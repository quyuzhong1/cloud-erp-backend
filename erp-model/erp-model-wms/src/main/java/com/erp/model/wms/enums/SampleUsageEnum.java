package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 样品用途枚举
 */
public enum SampleUsageEnum implements EnumMessage {
    OFFICE_USE("officeUse", "办公领用"),
    PHOTOGRAPHY("photography", "拍摄"),
    RESEARCH_DEVELOPMENT("researchDevelopment", "研发"),
    DOUYIN_LIVE("douyinLive", "抖音直播"),
    CUSTOMER_USE("customerUse", "客户领用（客户使用指导）"),
    EXHIBITION("exhibition", "参展"),
    MARKETING_SAMPLE("marketingSample", "营销样品"),
    CERTIFICATION_TESTING("certificationTesting", "认证检测"),
    SUPPLY_CHAIN_PRODUCTION("supplyChainProduction", "供应链生产组装"),
    USER_EXPERIENCE("userExperience", "用户新品体验（仓库提供）"),
    DEFECT_ANALYSIS("defectAnalysis", "不良品分析（从售后仓领样）"),
    GALAXY_OFFLINE_STORE("galaxyOfflineStore", "星河线下店领用"),
    OTHER("other", "其他")
    ;

    @EnumValue
    @JsonValue
    private String usage;
    private String name;

    SampleUsageEnum(String usage, String name) {
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
            for (SampleUsageEnum item : SampleUsageEnum.values()) {
                if (usage.equals(item.getUsage())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
    public static String getUsageByName(String name) {
        if (StringUtils.isNotBlank(name)) {
            for (SampleUsageEnum item : SampleUsageEnum.values()) {
                if (name.equals(item.getName())) {
                    return item.getUsage();
                }
            }
        }
        return "";
    }

    public static SampleUsageEnum getByUsage(String usage) {
        return Arrays.stream(values()).filter(a -> a.getUsage().equals(usage))
                .findFirst().orElse(null);
    }

    public static List<String> getUsageList() {
        return Arrays.stream(SampleUsageEnum.values()).map(SampleUsageEnum::getUsage).collect(Collectors.toList());
    }

    public static List<String> getNameList() {
        return Arrays.stream(SampleUsageEnum.values()).map(SampleUsageEnum::getName).collect(Collectors.toList());
    }
}
