package com.erp.model.plm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 任务依赖关系枚举
 *
 * @Author Cloud
 * @Date 2023/2/27 11:20
 **/
public enum TaskRelationshipEnum{

    FINISH_FINISH("ff", "完成-完成"),
    FINISH_START("fs", "完成-开始"),
    START_START("ss", "开始-开始"),
    START_FINISH("sf", "开始-完成"),
    ;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @EnumValue
    private String code;
    private String name;

    TaskRelationshipEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TaskRelationshipEnum getByCode(String code){
        return Arrays.stream(values()).filter(x -> x.getCode().equals(code))
                .findFirst().orElse(null);
    }

}
