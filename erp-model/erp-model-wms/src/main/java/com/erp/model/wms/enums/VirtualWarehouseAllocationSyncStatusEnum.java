package com.erp.model.wms.enums;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 虚拟仓分货单同步状态
 *
 * @author hyj
 */
public enum VirtualWarehouseAllocationSyncStatusEnum {

    NO_NEED_SYNC("0", "无需同步"),
    TO_BE_SYNC("1", "待同步"),
    IN_SYNC("2", "同步中"),
    SUCCESS_SYNC("3", "同步成功"),
    FAILED_SYNC("4", "同步失败"),
    MANUAL_COMPLETION_SYNC("5", "手动完结"),
    ;

    private String code;

    private String name;


    VirtualWarehouseAllocationSyncStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getCodeBySendStatus(Integer status) {
        if (0 == status) {
            return VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode();
        }
        if (1 == status) {
            return VirtualWarehouseAllocationSyncStatusEnum.SUCCESS_SYNC.getCode();
        }
        return VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode();
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
        VirtualWarehouseAllocationSyncStatusEnum syncStatusEnum = Arrays.stream(VirtualWarehouseAllocationSyncStatusEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
        return Optional.ofNullable(syncStatusEnum).map(VirtualWarehouseAllocationSyncStatusEnum::getName).orElse("");
    }

}
