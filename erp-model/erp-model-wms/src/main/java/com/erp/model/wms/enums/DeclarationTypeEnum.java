package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * @Author: wtr
 * @Date: 2025/11/13 11:07
 * @Param:
 * @Return:
 * @Description:
 **/
@Getter
public enum DeclarationTypeEnum implements EnumMessage {

    INDEPENDENT_DECLARATION("independentDeclaration","独立报关"),
    NON_INDEPENDENT_DECLARATION("nonIndependentDeclaration","非独立报关"),
    ;

    DeclarationTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
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

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * 通过code查询
     * DeliverTypeEnum
     * 枚举
     */
    public static DeclarationTypeEnum getByCode(String code) {
        return Stream.of(DeclarationTypeEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
