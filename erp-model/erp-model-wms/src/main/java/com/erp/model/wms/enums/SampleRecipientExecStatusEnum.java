package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 样品领用单明细执行状态枚举
 */
public enum SampleRecipientExecStatusEnum implements EnumMessage {
    WAIT_OUTSTOCK("waitOutstock", "待出库"),
    PART_OUTSTOCK("partOutstock", "部分出库"),
    COMPLETE_OUTSTOCK("completeOutstock", "已出库")
    ;

    @EnumValue
    @JsonValue
    private String execStatus;
    private String name;

    SampleRecipientExecStatusEnum(String execStatus, String name) {
        this.execStatus = execStatus;
        this.name = name;
    }

    public String getExecStatus() {
        return execStatus;
    }

    @Override
    public String getCode() {
        return execStatus;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String execStatus) {
        if (StringUtils.isNotBlank(execStatus)) {
            for (SampleRecipientExecStatusEnum item : SampleRecipientExecStatusEnum.values()) {
                if (execStatus.equals(item.getExecStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static SampleRecipientExecStatusEnum getByExecStatus(String execStatus) {
        return Arrays.stream(values()).filter(a -> a.getExecStatus().equals(execStatus))
                .findFirst().orElse(null);
    }

    public static List<String> getExecStatusList() {
        return Arrays.stream(SampleRecipientExecStatusEnum.values()).map(SampleRecipientExecStatusEnum::getExecStatus).collect(Collectors.toList());
    }

    public static List<String> getNameList() {
        return Arrays.stream(SampleRecipientExecStatusEnum.values()).map(SampleRecipientExecStatusEnum::getName).collect(Collectors.toList());
    }
}
