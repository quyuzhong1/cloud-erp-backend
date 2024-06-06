package com.erp.model.wms.enums;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 虚拟仓分货单状态
 *
 * @author hyj
 */
public enum VirtualWarehouseAllocationStatusEnum {

    WAIT_SUBMIT("waitSubmit", "待提交"),
    HANDLE("handle", "已处理"),
    INVALID("invalid", "已作废");

    private String code;
    private String name;

    VirtualWarehouseAllocationStatusEnum(String code, String name) {
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
        VirtualWarehouseAllocationStatusEnum allocationStatusEnum = Arrays.stream(VirtualWarehouseAllocationStatusEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
        return Optional.ofNullable(allocationStatusEnum).map(VirtualWarehouseAllocationStatusEnum::getName).orElse("");
    }
}
