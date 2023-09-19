package com.erp.model.bi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 考核维度
 *
 * @author Lambda
 * @Classname MetricsEnums
 * @Description 销售单价分布 国内国外区分
 * @Date 2023-09-13 10:38
 * @Created by yl
 */
public enum SaleContryTypeEnum implements EnumMessage {
    ABROAD("abroad", "国外"),
    DOMESTIC("domestic", "国内");
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public String code;
    /**
     * 名称
     */
    private String name;

    SaleContryTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static List<String> listName() {
        List<String> nameList = new ArrayList<>(6);
        for (SaleContryTypeEnum item : values()) {
            nameList.add(item.name);
        }
        return nameList;
    }


    public static SaleContryTypeEnum getByName(String metricsName) {
        for (SaleContryTypeEnum item : SaleContryTypeEnum.values()) {
            if (metricsName.equals(item.getName())) {
                return item;
            }
        }
        return null;
    }

    public static String getNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SaleContryTypeEnum item : SaleContryTypeEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
