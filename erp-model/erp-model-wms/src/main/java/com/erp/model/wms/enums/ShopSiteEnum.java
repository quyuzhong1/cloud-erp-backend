package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 单据类型
 */
public enum ShopSiteEnum implements EnumMessage {
    COM("com","agimbalgear",  "shopify"),
    JP("jp","ulanzijp",  "shopify"),
    DE("de","ulanzis",  "shopify"),
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


    ShopSiteEnum(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
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

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (ShopSiteEnum billTypeEnum : ShopSiteEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }


    public static List<ShopSiteEnum> listByType(String type) {
        List<ShopSiteEnum> list = new ArrayList<>();
        for (ShopSiteEnum billTypeEnum : ShopSiteEnum.values()) {
            if (type.equals(billTypeEnum.getType())) {
                list.add(billTypeEnum);
            }
        }
        return list;
    }
}
