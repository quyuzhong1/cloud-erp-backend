package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件导入状态
 * @author will
 * @date 2026/1/20 10:45
 */
public enum ImportHistoryRecordStatusEnum implements EnumMessage  {
    WAIT_HANDLE("waitHandle", "待处理"),
    HANDLE_ING("handleIng", "处理中"),
    HANDLE("handle", "已完成");

    @EnumValue
    @JsonValue
    private String status;
    private String name;

    ImportHistoryRecordStatusEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }


    public String getStatus() {
        return status;
    }
    @Override
    public String getCode() {
        return status;
    }
    @Override
    public String getName() {
        return name;
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (ImportHistoryRecordStatusEnum item : ImportHistoryRecordStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static ImportHistoryRecordStatusEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equals(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(ImportHistoryRecordStatusEnum.values()).map(ImportHistoryRecordStatusEnum::getStatus).collect(Collectors.toList());
    }

}
