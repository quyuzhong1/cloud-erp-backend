package com.erp.model.wms.enums;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 条码枚举类
 * @author: ZDY
 */
public enum BarcodeSizeEnum implements EnumMessage {
    X52("5X2", "5*2cm","5","2","CENTIMETER"),
    X53("5X3", "5*2cm","5","2","CENTIMETER"),
    X73("7X3", "5*2cm","5","2","CENTIMETER"),
    X72("7X2", "5*2cm","5","2","CENTIMETER"),
    X82("8X2", "5*2cm","5","2","CENTIMETER"),
    X83("8X3", "5*2cm","5","2","CENTIMETER"),
    ;

    @EnumValue
    @JsonValue
    private String status;
    private String name;
    private String width;
    private String height;
    private String unit;

    BarcodeSizeEnum(String status, String name, String width, String height, String unit) {
        this.status = status;
        this.name = name;
        this.width = width;
        this.height = height;
        this.unit = unit;
    }


    public String getStatus() {
        return status;
    }
    @Override
    public String getCode() {
        return status;
    }
    @Override
    public String getName() {
        return name;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }

    public String getUnit() {
        return unit;
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (BarcodeSizeEnum item : BarcodeSizeEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static BarcodeSizeEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equals(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(BarcodeSizeEnum.values()).map(BarcodeSizeEnum::getStatus).collect(Collectors.toList());
    }
}
