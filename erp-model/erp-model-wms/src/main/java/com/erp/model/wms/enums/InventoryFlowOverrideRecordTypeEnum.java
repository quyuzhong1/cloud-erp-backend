package com.erp.model.wms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 库存流水重算时间范围记录 类型 枚举
 * </p>
 *
 * @author cloud
 * @since 2024-08-09 16:07:19
 */
public enum InventoryFlowOverrideRecordTypeEnum implements EnumMessage {
	AUTO("auto", "自动生成"),
	MANUAL("manual", "手动触发"),
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

    InventoryFlowOverrideRecordTypeEnum(String code, String name) {
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
        for (InventoryFlowOverrideRecordTypeEnum statusEnum : InventoryFlowOverrideRecordTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
