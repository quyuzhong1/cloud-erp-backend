package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 外部系统 系统代码 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-06-11 09:37:12
 */
public enum DmpBasicSystemCodeEnum implements EnumMessage {
	AMAZON("amazon", "亚马逊"),
	KINGDEE("kingdee", "金蝶"),
	WDT("wdt", "旺店通"),
	MABANG("mabang", "马帮"),
    TIKTOK("TikTok", "TikTok"),
	ALI_EXPRESS("AliExpress", "速卖通"),
	SHOPIFY("Shopify", "Shopify"),
    MERCADOLIBRE("mercadolibre", "美客多"),
    GOODCANG("goodcang", "谷仓"),
    IML("iml", "艾姆勒"),
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

    DmpBasicSystemCodeEnum(String code, String name) {
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
        for (DmpBasicSystemCodeEnum statusEnum : DmpBasicSystemCodeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
