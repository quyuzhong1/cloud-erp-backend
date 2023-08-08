package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 盘点状态枚举
 * @author Cloud
 */
public enum StocktakingStatusEnum implements EnumMessage {


    NOT_STARTED("notStarted", "未开始"),
    IN_PROGRESS("inProgress", "盘点中"),
    COMPLETED("completed", "完成"),
    RECOUNT("recount", "复盘");

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

    StocktakingStatusEnum(String code, String name) {
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

    public static StocktakingStatusEnum getByCode(String code) {
        return Arrays.stream(StocktakingStatusEnum.values())
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
