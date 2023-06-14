package com.erp.model.sys.enums;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @ClassName UserRangeTypeEnum
 * @Author: zhangchunlin
 * @Date: 2023/6/12 14:40
 * @Description: 用户区间类型枚举
 */ 

public enum UserRangeTypeEnum {

    INVENTORY_AGE("inventoryAge", "库龄天数", "{}-{}天"),;


    private String code;
    private String name;
    private String labelFormat;

    UserRangeTypeEnum(String code, String name, String labelFormat) {
        this.code = code;
        this.name = name;
        this.labelFormat = labelFormat;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getLabelFormat() {
        return labelFormat;
    }

    /**
     * 根据代码获取
     * @param code
     * @return
     */
    public static UserRangeTypeEnum of(String code) {
        return Arrays.stream(UserRangeTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 根据代码获取名称
     * @param code
     * @return
     */
    public static String getNameByCode(String code) {
        UserRangeTypeEnum userRangeTypeEnum = of(code);
        return Optional.ofNullable(userRangeTypeEnum).map(UserRangeTypeEnum::getName).orElse("");
    }

    /**
     * 根据代码获取标题
     * @param code
     * @return
     */
    public static String getLabelFormatByCode(String code) {
        UserRangeTypeEnum userRangeTypeEnum = of(code);
        return Optional.ofNullable(userRangeTypeEnum).map(UserRangeTypeEnum::getLabelFormat).orElse("");
    }

}
