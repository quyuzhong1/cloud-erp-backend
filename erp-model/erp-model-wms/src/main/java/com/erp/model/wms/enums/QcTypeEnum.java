package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 质检类型枚举
 *
 * @author Lambda
 * @Classname QcTypeEnum
 * @Description TODO
 * @Date 2023-04-13 10:34
 * @Created by yl
 */
public enum QcTypeEnum {
    STOCK_IN("stockIn", "入库质检",true),
    STOCK_OUT("stockOut", "出库质检",true),
    OUTSIDE_QC("outsideQc", "外检质检",false),
    INSIDE_QC("insideQc", "在库质检",true),
    NEW_PRODUCT_STOCK_IN("newProductStockIn", "新品入库质检",true),
    B2B_OUTSIDE_QC("b2bOutsideQc", "B2B外检",false),
    RETURN_QC("returnQc", "退货质检",false);


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
     * 是否内检
     */
    private Boolean isInside;

    QcTypeEnum(String code, String name, Boolean isInside) {
        this.code = code;
        this.name = name;
        this.isInside = isInside;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Boolean getIsInside() {
        return isInside;
    }


    public static String getByCode(String code) {
        QcTypeEnum qcTypeEnum = Arrays.stream(values()).filter(p -> p.getCode().equals(code))
                .findFirst().orElse(null);
        if (qcTypeEnum != null) {
            return qcTypeEnum.getName();
        }
        return "";
    }

    public static Boolean getIsInsideByCode(String code) {
        QcTypeEnum qcTypeEnum = Arrays.stream(values()).filter(p -> p.getCode().equals(code))
                .findFirst().orElse(null);
        if (qcTypeEnum != null) {
            return qcTypeEnum.getIsInside();
        }
        return false;
    }
}
