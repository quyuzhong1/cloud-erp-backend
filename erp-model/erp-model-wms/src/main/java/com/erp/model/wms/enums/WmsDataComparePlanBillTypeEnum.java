package com.erp.model.wms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 数据对比映射方案 单据类型 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-03-20 17:21:53
 */
public enum WmsDataComparePlanBillTypeEnum implements EnumMessage {
	SOOUTSTOCK("soOutstock", "销售出库单"),
	FBASHIPMENT("fbaShipment", "FBA货件签收"),
	OVERSEASINBOUND("overseasInbound", "第三方仓货件签收"),
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

    WmsDataComparePlanBillTypeEnum(String code, String name) {
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
        for (WmsDataComparePlanBillTypeEnum statusEnum : WmsDataComparePlanBillTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
