package com.erp.model.bi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考核维度
 *
 * @author Lambda
 * @Classname MetricsEnums
 * @Description TODO
 * @Date 2023-09-13 10:38
 * @Created by yl
 */
public enum MetricsEnum implements EnumMessage {


    SALES_AMOUNT("salesAmount", "销售额"),
    FINANCE_SALES_AMOUNT("financeSalesAmount", "财务销售额"),
    NET_SALES_AMOUNT("netSalesAmount", "净销售额"),
    SALES_QTY("salesQty", "销量"),
    GROSS_PROFIT("grossProfit", "毛利额度"),
    GROSS_PROFIT_RATE("grossProfitRate", "毛利率");

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

    MetricsEnum(String code, String name) {
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
        for (MetricsEnum item : values()) {
            nameList.add(item.name);
        }
        return nameList;
    }


    public static MetricsEnum getByName(String metricsName) {
        for (MetricsEnum item : MetricsEnum.values()) {
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
        for (MetricsEnum item : MetricsEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
