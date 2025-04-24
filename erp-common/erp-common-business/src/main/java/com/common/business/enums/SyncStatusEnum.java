package com.common.business.enums;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/9 8:59
 */
public enum SyncStatusEnum {

    NO_NEED_SYNC("0", "无需同步"),
    TO_BE_SYNC("1", "待同步"),
    IN_SYNC("2", "同步中"),
    SUCCESS_SYNC("3", "同步成功"),
    FAILED_SYNC("4", "同步失败");

    private String code;

    private String name;


    SyncStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getCodeBySendStatus(Integer status) {
        if( 0 == status){
            return SyncStatusEnum.FAILED_SYNC.getCode();
        }
        if (1== status) {
            return SyncStatusEnum.SUCCESS_SYNC.getCode();
        }
        return SyncStatusEnum.FAILED_SYNC.getCode();
    }

    public static SyncStatusEnum getByDmpInputTaskStatus(String dmpInputTaskStatus) {
        if ("error".equalsIgnoreCase(dmpInputTaskStatus)){
            return FAILED_SYNC;
        }
        if ("finish".equalsIgnoreCase(dmpInputTaskStatus)){
            return SUCCESS_SYNC;
        }
        return IN_SYNC;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    /**
     * 根据代码获取名称
     * @param code
     * @return
     */
    public static String getNameByCode(String code) {
        SyncStatusEnum syncStatusEnum = Arrays.stream(SyncStatusEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
        return Optional.ofNullable(syncStatusEnum).map(SyncStatusEnum::getName).orElse("");
    }

}
