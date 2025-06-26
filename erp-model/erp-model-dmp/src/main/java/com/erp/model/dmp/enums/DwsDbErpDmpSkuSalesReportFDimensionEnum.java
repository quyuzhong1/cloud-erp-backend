package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * SKU销量报告 统计维度 枚举
 * </p>
 *
 * @author Jim
 * @since 2025-06-23 11:58:53
 */
public enum DwsDbErpDmpSkuSalesReportFDimensionEnum implements EnumMessage {
	DELIVERYTIME("deliveryTime", "出库时间"),
	PAYMENTTIME("paymentTime", "付款时间"),
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

    DwsDbErpDmpSkuSalesReportFDimensionEnum(String code, String name) {
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
        for (DwsDbErpDmpSkuSalesReportFDimensionEnum statusEnum : DwsDbErpDmpSkuSalesReportFDimensionEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
