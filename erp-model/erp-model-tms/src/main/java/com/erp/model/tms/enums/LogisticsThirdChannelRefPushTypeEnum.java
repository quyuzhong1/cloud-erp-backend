package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 物流-第三方渠道关系表 推送类型 枚举
 * </p>
 *
 * @author zdy
 * @since 2025-07-30 12:08:08
 */
public enum LogisticsThirdChannelRefPushTypeEnum implements EnumMessage {
	SENDER("sender", "按照默认发件人"),
	RECEIVER("receiver", "按照默认收件人"),
	ORDER_RECEIVER("orderReceiver", "按照默认订单收件人"),
	SHOP_SENDER("shopSender", "按照默认发件人-店铺"),
	PLATFORM_SENDER("platformSender", "按照默认发件人-平台"),
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

    LogisticsThirdChannelRefPushTypeEnum(String code, String name) {
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
        for (LogisticsThirdChannelRefPushTypeEnum statusEnum : LogisticsThirdChannelRefPushTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
    public static String getCodeByName(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (LogisticsThirdChannelRefPushTypeEnum statusEnum : LogisticsThirdChannelRefPushTypeEnum.values()) {
            if (name.equals(statusEnum.getName())) {
                return statusEnum.getCode();
            }
        }
        return "";
    }
}
