package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 头程调整记录 调整分类 枚举
 * </p>
 *
 * @author zdy
 * @since 2025-05-12 15:19:23
 */
public enum FirstMileChangeRecordCategoryEnum implements EnumMessage {
	BOXNO("boxNo", "箱号"),
	SHIPPINGCOST("shippingCost", "运费"),
	DECLARECOST("declareCost", "关税"),
	OTHERTAXFEE("otherTaxFee", "其他税费"),
	OTHERCOST("otherCost", "其他费用"),
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

    FirstMileChangeRecordCategoryEnum(String code, String name) {
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
        for (FirstMileChangeRecordCategoryEnum statusEnum : FirstMileChangeRecordCategoryEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
