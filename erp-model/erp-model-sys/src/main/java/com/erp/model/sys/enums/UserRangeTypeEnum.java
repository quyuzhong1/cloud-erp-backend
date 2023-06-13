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

    ITEM_MANAGER("projectCharge", "项目经理"),
    PRODUCT_MANAGER("productCharge", "产品经理");


    private String code;
    private String name;

    UserRangeTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
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

}
