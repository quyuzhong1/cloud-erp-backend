package com.erp.model.scm.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 销量设置 统计维度 枚举
 * </p>
 *
 * @author jack
 * @since 2025-06-13 10:54:36
 */
public enum CfgSupplierSalesDisplayFieldEnum implements EnumMessage {
    SALE_STATE("saleState", "销售状态"),
    SALEABLE_STOCK("saleableStock", "可销库存"),
    DAILY_SALES("dailySales", "日均销量"),
    SALES_LAST_3_DAYS("salesLast3Days", "近3日销量"),
    SALES_LAST_7_DAYS("salesLast7Days", "近7日销量"),
    SALES_LAST_30_DAYS("salesLast30Days", "近30日销量"),
    SALES_LAST_60_DAYS("salesLast60Days", "近60日销量"),
    SALES_LAST_90_DAYS("salesLast90Days", "近90日销量"),
    SALEABLE_DAYS("saleableDays", "可销天数");
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

    CfgSupplierSalesDisplayFieldEnum(String code, String name) {
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

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgSupplierSalesDisplayFieldEnum statusEnum : CfgSupplierSalesDisplayFieldEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
