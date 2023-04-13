package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 质检类型枚举
 * @author Lambda
 * @Classname QcTypeEnum
 * @Description TODO
 * @Date 2023-04-13 10:34
 * @Created by yl
 */
public enum QcTypeEnum implements BaseEnum {
    STOCK_IN("stockIn", "入库质检"),
    OUTSIDE_QC("outsideQc", "外检质检"),
    INSIDE_QC("insideQc", "在库质检"),
    NEW_PRODUCT_STOCK_IN("newProductStockIn", "新品入库质检"),
    B2B_OUTSIDE_QC("b2bOutsideQc", "B2B外检");



    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String type;
    /**
     * 名称
     */
    private String name;

    QcTypeEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public Object getCode() {
        return type;
    }


    public static String getTypeName(String type) {
        QcTypeEnum qcTypeEnum = Arrays.stream(values()).filter(p -> p.getType().equals(type))
                .findFirst().orElse(null);
        if (qcTypeEnum != null) {
            return qcTypeEnum.getName();
        }
        return "";

    }
}
