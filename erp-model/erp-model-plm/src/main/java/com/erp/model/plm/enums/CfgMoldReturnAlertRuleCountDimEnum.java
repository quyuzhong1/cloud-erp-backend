package com.erp.model.plm.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 模具返还策略 标准 枚举
 * </p>
 *
 * @author jack
 * @since 2025-10-15 17:07:31
 */
public enum CfgMoldReturnAlertRuleCountDimEnum implements EnumMessage {
	PURCHASEORDER("purchaseOrder", "以采购下单数量"),
	WAREHOUSERECEIVE("warehouseReceive", "以采购收货数量"),
	POINSTOCK("poInstock", "以采购入库数量"),
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

    CfgMoldReturnAlertRuleCountDimEnum(String code, String name) {
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
        for (CfgMoldReturnAlertRuleCountDimEnum statusEnum : CfgMoldReturnAlertRuleCountDimEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
    public static String getCode(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (CfgMoldReturnAlertRuleCountDimEnum statusEnum : CfgMoldReturnAlertRuleCountDimEnum.values()) {
            if (name.equals(statusEnum.getName())) {
                return statusEnum.getCode();
            }
        }
        return "";
    }
}
