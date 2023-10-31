package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * FBA发货状态
 * @Author Luo_WG
 * @Date 2023/10/31 16:01
 **/
public enum FbaDeliveryStatusEnum implements EnumMessage {
    SHIPPED("shipped", "已发货"),
    IS_OVER("isOver", "已完结"),
    UN_SHIPPED("unShipped", "未发货"),
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

    FbaDeliveryStatusEnum(String code, String name) {
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
        if (StringUtils.isNotBlank(code)) {
            for (FbaDeliveryStatusEnum item : FbaDeliveryStatusEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
