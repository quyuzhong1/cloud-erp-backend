package com.erp.model.scm.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 销量设置条件明细 配置类型 枚举
 * </p>
 *
 * @author jack
 * @since 2025-06-13 10:54:36
 */
public enum CfgSupplierSalesConditionTypeEnum implements EnumMessage {
	SKU("sku", "sku查看配置"),
	SALEABLESTOCK("saleableStock", "可销库存配置"),
	SALESSTATISTIC("salesStatistic", "销量统计配置"),
	NOTICE("notice", "通知配置"),
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

    CfgSupplierSalesConditionTypeEnum(String code, String name) {
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
        for (CfgSupplierSalesConditionTypeEnum statusEnum : CfgSupplierSalesConditionTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
