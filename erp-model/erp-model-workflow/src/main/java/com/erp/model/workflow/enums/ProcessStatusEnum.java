package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.enums.FsApproveStatusEnum;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 *
 *
 * @Author Cloud
 * @Date 2023/4/26 16:47
 **/
public enum ProcessStatusEnum {
    // 流程暂停
    PAUSE("pause", "暂停"),
    // 已终止
    TERMINATION("termination", "已终止"),
    // 已完成
    FINISH("finish", "已完成"),
    // 运行中
    RUNNING("running", "运行中"),
    ;
    @EnumValue
    @JsonValue
    private final String code;
    private final String name;

    ProcessStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static ProcessStatusEnum getByCode(String code) {
        return Arrays.stream(ProcessStatusEnum.values())
                .filter(state -> code.equals(state.getCode()))
                .findFirst().orElse(null);
    }

    /**
     * 根据飞书状态转换
     * @author will
     * @date 2025/7/17 09:59
     * @param fsApproveStatusEnum
     * @return ProcessStatusEnum
     */
    public static String getByCode(FsApproveStatusEnum fsApproveStatusEnum) {
        if (FsApproveStatusEnum.PENDING.equals(fsApproveStatusEnum)) {
            return RUNNING.getCode();
        }
        if (FsApproveStatusEnum.CANCELED.equals(fsApproveStatusEnum) || FsApproveStatusEnum.REJECTED.equals(fsApproveStatusEnum)) {
            return TERMINATION.getCode();
        }
        if (FsApproveStatusEnum.TRANSFERRED.equals(fsApproveStatusEnum) || FsApproveStatusEnum.APPROVED.equals(fsApproveStatusEnum) || FsApproveStatusEnum.DONE.equals(fsApproveStatusEnum)) {
            return FINISH.getCode();
        }
        return "";
    }
}
