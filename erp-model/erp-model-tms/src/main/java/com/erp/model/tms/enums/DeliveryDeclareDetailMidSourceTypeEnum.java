package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 报关明细中间表 单据分类 枚举
 * </p>
 *
 * @author jack
 * @since 2026-04-27 15:53:56
 */
public enum DeliveryDeclareDetailMidSourceTypeEnum implements EnumMessage {
	FIRSTMILEDELIVERY("firstMileDelivery", "头程发货单"),
	SODELIVERYNOTICE("soDeliveryNotice", "B2B发货通知单"),
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

    DeliveryDeclareDetailMidSourceTypeEnum(String code, String name) {
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
        for (DeliveryDeclareDetailMidSourceTypeEnum statusEnum : DeliveryDeclareDetailMidSourceTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
