package com.erp.model.sys.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 组织职能 枚举
 * </p>
 *
 * @author jack
 * @since 2025-05-23 09:29:51
 */
public enum OrgFunctionsEnum implements EnumMessage {
	PRODUCTION("production", "生产组织"),
	PURCHASE("purchase", "采购组织"),
	INVENTORY("inventory", "库存组织"),
	OWNER("owner", "货主组织"),
	CUSTOMS("customs", "报关组织"),
	OVERSEAS_SALES("overseas_sales", "国外销售组织"),
	DOMESTIC_SALES("domestic_sales", "国内销售组织"),
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

    OrgFunctionsEnum(String code, String name) {
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
        for (OrgFunctionsEnum statusEnum : OrgFunctionsEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
