package com.erp.model.wms.enums;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @Classname: TransitOwnerEnum
 * @Description: 在途归属
 * @CreateTime: 2023-05-26  09:07
 * @Author: zhangchunlin
 */
public enum TransitOwnerEnum {

    TRANSFER_IN("in", "调入方"),
    TRANSFER_OUT("out", "调出方"),
    ;

    /**
     * 类型
     */
    private String code;
    /**
     * 名称
     */
    private String name;

    TransitOwnerEnum(String code, String name) {
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
    public static TransitOwnerEnum of(String code) {
        return Arrays.stream(TransitOwnerEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 根据代码获取名称
     * @param code
     * @return
     */
    public static String getNameByCode(String code) {
        TransitOwnerEnum transitOwnerEnum =  of(code);
        return Optional.ofNullable(transitOwnerEnum).map(TransitOwnerEnum::getName).orElse("");
    }

}
