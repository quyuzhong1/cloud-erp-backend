package com.erp.model.wms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 单据类型 枚举
 * </p>
 *
 * @author zdy
 * @since 2026-03-19 19:02:56
 */
public enum WmsPackingSourceTypeEnum implements EnumMessage {
	B2B("B2B", "B2B"),
	FBA("FBA", "FBA"),
	THIRDWAREHOUSE("thirdWarehouse", "第三方仓"),
	ALIEXPRESS("aliexpress", "速卖通仓"),
	FBT("fbt", "FBT"),
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

    WmsPackingSourceTypeEnum(String code, String name) {
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
        for (WmsPackingSourceTypeEnum statusEnum : WmsPackingSourceTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
