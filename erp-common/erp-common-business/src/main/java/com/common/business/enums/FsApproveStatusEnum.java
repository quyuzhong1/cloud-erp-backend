package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 飞书审核没加
 * @author will
 * @date 2025/6/30 14:23
 */
@Getter
public enum FsApproveStatusEnum {

    PENDING("PENDING", "审批中"),
    CANCELED("CANCELED", "取消审核"),
    APPROVED("APPROVED", "审批通过"),
    REJECTED("REJECTED", "审批不通过"),
    TRANSFERRED("TRANSFERRED", "任务转交"),
    DONE("DONE", "任务通过"),

    ;
    @JsonValue
    @EnumValue
    private final String status;
    private final String name;

    FsApproveStatusEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }


    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (FsApproveStatusEnum item : FsApproveStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static FsApproveStatusEnum getByCode(String code) {
        return Arrays.stream(FsApproveStatusEnum.values())
                .filter(e -> e.getStatus().equals(code))
                .findFirst()
                .orElse(null);
    }

}
