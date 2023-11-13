package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 单位枚举
 * @author Lambda
 * @Classname UnitEnum
 * @Description TODO
 * @Date 2023-11-13 15:01
 * @Created by yl
 */
public enum UnitEnum implements EnumMessage {
    DAY("day","天","timeUnit" ,""),
    G("g","克","weightUnit", ""),
    KG("kg","千克","weightUnit" ,""),
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

    /**
     * 类型
     */
    private String type;

    /**
     * 描述
     */
    private String desc;

    UnitEnum(String code,String name,String type,String desc){
        this.code = code;
        this.name = name;
        this.type = type;
        this.desc = desc;
    }
    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }
    public String getType() {
        return this.type;
    }

    public String getDesc() {
        return this.desc;
    }


    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (UnitEnum item : UnitEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
