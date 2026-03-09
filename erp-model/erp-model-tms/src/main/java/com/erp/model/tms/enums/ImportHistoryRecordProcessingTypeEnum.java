package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件导入类型
 * @author will
 * @date 2026/1/20 10:45
 */
public enum ImportHistoryRecordProcessingTypeEnum implements EnumMessage  {
    PRE_PROCESSING("preprocessing", "预处理"),
    IMPORT("import", "正式导入"),
    CONFIRM_IMPORT("confirmImport", "导入确认");
    @EnumValue
    @JsonValue
    private String status;
    private String name;

    ImportHistoryRecordProcessingTypeEnum(String status, String name) {
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
            for (ImportHistoryRecordProcessingTypeEnum item : ImportHistoryRecordProcessingTypeEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static ImportHistoryRecordProcessingTypeEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equals(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(ImportHistoryRecordProcessingTypeEnum.values()).map(ImportHistoryRecordProcessingTypeEnum::getStatus).collect(Collectors.toList());
    }

}
