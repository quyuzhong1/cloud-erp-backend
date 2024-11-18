package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lambda
 * @Classname PackageForecastCollectModeEnum
 * @Description 组包预报揽收方式
 * @Date 2024-02-19 11:23
 * @Created by yl
 */
public enum PackageForecastCollectModeEnum implements EnumMessage {
    TO_HOME("toHome", "上门揽收"),
    SELF_SEND("selfSend", "自送"),
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

    PackageForecastCollectModeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (PackageForecastCollectModeEnum item : PackageForecastCollectModeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
