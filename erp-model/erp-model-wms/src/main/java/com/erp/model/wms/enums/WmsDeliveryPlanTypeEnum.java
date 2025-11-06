package com.erp.model.wms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 发货计划类型 枚举
 * </p>
 *
 * @author shukai
 * @since 2025-10-13 17:56:03
 */
public enum WmsDeliveryPlanTypeEnum implements EnumMessage {
	FBA("fba", "FBA发货计划"),
	THIRDWAREHOUSE("thirdWarehouse", "第三方仓发货计划"),
	ALIEXPRESS("AliExpress", "速卖通发货计划"),
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

    WmsDeliveryPlanTypeEnum(String code, String name) {
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
        for (WmsDeliveryPlanTypeEnum statusEnum : WmsDeliveryPlanTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
