package com.erp.model.plm.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 模具预警策略 标准 枚举
 * </p>
 *
 * @author jack
 * @since 2025-10-20 10:27:11
 */
public enum CfgMoldAlertRuleCountDimEnum implements EnumMessage {
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

    CfgMoldAlertRuleCountDimEnum(String code, String name) {
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
        for (CfgMoldAlertRuleCountDimEnum statusEnum : CfgMoldAlertRuleCountDimEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
