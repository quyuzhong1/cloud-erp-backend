package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;


/**
 * <p>
 * 仓库仓位分区类型
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-05-22
 */
public enum WarehouseLocationTypeEnum implements EnumMessage {

    LOCATION ("location", "仓位"),
    AREA("area", "分区"),
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

    WarehouseLocationTypeEnum(String code, String name) {
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

    /**
     * 根据代码获取
     * @param code
     * @return
     */
    public static WarehouseLocationTypeEnum of(String code) {
        return Arrays.stream(WarehouseLocationTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 根据代码获取名称
     * @param code
     * @return
     */
    public static String getName(String code) {
        WarehouseLocationTypeEnum WarehouseLocationType =  of(code);
        return Optional.ofNullable(WarehouseLocationType).map(WarehouseLocationTypeEnum::getName).orElse("");
    }

}
