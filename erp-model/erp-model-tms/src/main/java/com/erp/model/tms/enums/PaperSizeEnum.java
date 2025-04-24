package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 纸张大小枚举
 * @author Lambda
 * @Classname PaperSizeEnum
 * @Date 2023-11-13 14:42
 * @Created by yl
 */
public enum PaperSizeEnum implements EnumMessage {
    MULTIPLY_100_100("100*100","100mm*100mm",100,100),
    MULTIPLY_100_150("100*150","100mm*100mm",150,100),
    A4("A4","A4",297,210)
    ;

    @EnumValue
    @JsonValue
    private String code;


    private String name;

    /**
     * 长
     */
    private Integer length;

    /**
     * 宽
     */
    private Integer width;


    PaperSizeEnum(String code, String name,Integer length,Integer width){
        this.code = code;
        this.name = name;
        this.length = length;
        this.width = width;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }



    public Integer getLength(){
        return this.length;
    }

    public Integer getWidth(){
        return this.width;
    }


    public static PaperSizeEnum getByCode(String code){
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (PaperSizeEnum item : PaperSizeEnum.values()) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
