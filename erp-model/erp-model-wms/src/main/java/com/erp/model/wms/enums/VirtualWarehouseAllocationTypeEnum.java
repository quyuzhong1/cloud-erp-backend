package com.erp.model.wms.enums;

import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 虚拟仓分货单类型：新增分货，1虚拟仓调拨，2取消分货
 *
 * @author hyj
 */
public enum VirtualWarehouseAllocationTypeEnum {

    ALLOCATION("allocation", "新增分货"),
    TRANSFER("transfer", "虚拟仓调拨"),
    CANCEL("cancel", "取消分货");

    private String code;
    private String name;

    VirtualWarehouseAllocationTypeEnum(String code, String name) {
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
     * 根据code获取名称
     *
     * @param code
     * @return
     */
    public static String getNameByCode(String code) {
        VirtualWarehouseAllocationTypeEnum typeEnum = Arrays.stream(VirtualWarehouseAllocationTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
        return Optional.ofNullable(typeEnum).map(VirtualWarehouseAllocationTypeEnum::getName).orElse("");
    }
}
