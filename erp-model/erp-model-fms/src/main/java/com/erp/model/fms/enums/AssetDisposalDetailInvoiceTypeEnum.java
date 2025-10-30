package com.erp.model.fms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 资产处置单资产明细表 发票类型 枚举
 * </p>
 *
 * @author jack
 * @since 2025-10-29 14:34:18
 */
public enum AssetDisposalDetailInvoiceTypeEnum implements EnumMessage {
    VALUE_ADDED_TAX("valueAddedTax", "增值税发票"),
    INVOICE("invoice", "普通发票"),
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

    AssetDisposalDetailInvoiceTypeEnum(String code, String name) {
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
        for (AssetDisposalDetailInvoiceTypeEnum statusEnum : AssetDisposalDetailInvoiceTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
