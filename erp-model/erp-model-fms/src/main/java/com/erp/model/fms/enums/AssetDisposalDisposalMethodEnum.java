package com.erp.model.fms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 资产处置单主表 处置方式 枚举
 * </p>
 *
 * @author jack
 * @since 2025-10-29 14:34:18
 */
public enum AssetDisposalDisposalMethodEnum implements EnumMessage {
	SCRAP("scrap", "报废"),
	LOSS("loss", "盘亏"),
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

    AssetDisposalDisposalMethodEnum(String code, String name) {
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
        for (AssetDisposalDisposalMethodEnum statusEnum : AssetDisposalDisposalMethodEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
