package com.erp.model.scm.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 销量设置 日均销量类型 枚举
 * </p>
 *
 * @author jack
 * @since 2025-06-13 10:54:36
 */
public enum CfgSupplierSalesDailySalesTypeEnum implements EnumMessage {
	DAILYAVG3DAYS("dailyAvg3Days", "按3天日均计算"),
	DAILYAVG7DAYS("dailyAvg7Days", "按7天日均计算"),
	DAILYAVG30DAYS("dailyAvg30Days", "按30天日均计算"),
	DAILYAVG60DAYS("dailyAvg60Days", "按60天日均计算"),
	DAILYAVG90DAYS("dailyAvg90Days", "按90天日均计算"),
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

    CfgSupplierSalesDailySalesTypeEnum(String code, String name) {
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
        for (CfgSupplierSalesDailySalesTypeEnum statusEnum : CfgSupplierSalesDailySalesTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
