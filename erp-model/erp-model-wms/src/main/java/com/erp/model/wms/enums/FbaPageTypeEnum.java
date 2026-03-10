package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.common.core.exception.ServiceException;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签类型
 */
public enum FbaPageTypeEnum implements EnumMessage {
    AWD_PLAIN_PAPER("PLAIN_PAPER", "每张美国信纸1个标签", "awdPageType"),
    AWD_THERMAL_NONPCP("THERMAL_NONPCP", "热敏纸一个标签", "awdPageType"),
    AWD_LETTER_6("LETTER_6", "每张美国信纸6个标签", "awdPageType"),
    FBA_PLAIN_PAPER("PackageLabel_Plain_Paper", "每张美国信纸1个标签", "fbaPageType"),
    FBA_LETTER_2("PackageLabel_Letter_2", "每张美国信纸2个标签", "fbaPageType"),
    FBA_LETTER_6("PackageLabel_Letter_6", "每张美国信纸6个标签", "fbaPageType"),
    FBA_A4_2("PackageLabel_A4_2", "每张A4纸上2个标签", "fbaPageType"),
    FBA_A4_4("PackageLabel_A4_4", "每张A4纸上4个标签", "fbaPageType"),
    FBA_THERMAL("PackageLabel_Thermal", "热敏纸(亚马逊合作承运人UPS)", "fbaPageType"),
    FBA_THERMAL_UNIFIED("PackageLabel_Thermal_Unified", "热敏纸(ATS发货)", "fbaPageType"),
    FBA_THERMAL_NO_CARRIER_ROTATION("PackageLabel_Thermal_No_Carrier_Rotation", "热敏纸(亚马逊合作承运人DHL)", "fbaPageType"),
    FBA_THERMAL_NONPCP("PackageLabel_Thermal_NonPCP", "热敏纸(非亚马逊合作物流)", "fbaPageType"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;


    FbaPageTypeEnum(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public static void validate(String pageType, String orderType) {
        if (StringUtils.isBlank(pageType)) {
            throw new ServiceException("标签类型不能为空");
        }
        if (StringUtils.isBlank(orderType)) {
            throw new ServiceException("订单类型不能为空");
        }
        // 校验标签类型是否支持该订单类型
        if (!listByType(orderType).contains(getEnumByCode(pageType))) {
            throw new ServiceException("订单类型【{}】不支持标签类型【{}】", orderType, pageType);

        }
    }


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public static FbaPageTypeEnum getEnumByCode(String code){
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (FbaPageTypeEnum billTypeEnum : FbaPageTypeEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum;
            }
        }
        return null;
    }
    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (FbaPageTypeEnum billTypeEnum : FbaPageTypeEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }


    public static List<FbaPageTypeEnum> listByType(String type) {
        List<FbaPageTypeEnum> list = new ArrayList<>();
        for (FbaPageTypeEnum billTypeEnum : FbaPageTypeEnum.values()) {
            if (type.equals(billTypeEnum.getType())) {
                list.add(billTypeEnum);
            }
        }
        return list;
    }
}
