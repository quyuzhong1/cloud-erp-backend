package com.erp.model.wms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 质检参考图片类型 枚举
 * </p>
 *
 * @author wtr
 * @since 2026-03-25 11:34:33
 */
public enum WmsQcStandardImageTypeEnum implements EnumMessage {
	PRODUCTPHYSICAL("productPhysical", "产品实物"),
	PACKAGINGACCESSORIES("packagingAccessories", "包装配件"),
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

    WmsQcStandardImageTypeEnum(String code, String name) {
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
        for (WmsQcStandardImageTypeEnum statusEnum : WmsQcStandardImageTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
