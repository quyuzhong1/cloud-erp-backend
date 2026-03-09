package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * @Author: wtr
 * @Date: 2026/2/3 11:29
 * @Param:
 * @Return:
 * @Description:
 **/
public enum PushStocktakingProfitLossStatusEnum implements EnumMessage {


    NOT_GENERATE("notGenerate", "未生成"),
    NOT_ALL_GENERATE("notAllGenerate", "未完全生成"),
    GENERATED("generated", "已生成"),
    NOT_NEED_GENERATE("notNeedGenerate", "无需生成");

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

    PushStocktakingProfitLossStatusEnum(String code, String name) {
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

    public static PushStocktakingProfitLossStatusEnum getByCode(String code) {
        return Arrays.stream(PushStocktakingProfitLossStatusEnum.values())
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}