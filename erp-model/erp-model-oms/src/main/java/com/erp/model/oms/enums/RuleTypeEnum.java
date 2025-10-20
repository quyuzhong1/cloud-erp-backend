package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * @author Lambda
 * @Classname TypeEnum
 * @Description TODO
 * @Date 2023-08-18 11:00
 * @Created by yl
 */
@Getter
public enum RuleTypeEnum implements EnumMessage {
    B2C_PLATFORM("platform","b2c平台"),
    B2B_PLATFORM("b2bPlatform","b2b平台"),
    WAREHOUSE("warehouse","仓库"),
    CUSTOMER("customer","客户"),
    ASSIGN("assign","指定物流"),
    MIN_FREIGHT("minFreight","最低运费")
    ;

    RuleTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    public final String code;
    /**
     * 名称
     */
    private final String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 通过code查询
     */
    public static RuleTypeEnum getByCode(String code){
        return Stream.of(RuleTypeEnum.values())
                .filter(typeEnum -> typeEnum.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (RuleTypeEnum typeEnum : RuleTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }
}
