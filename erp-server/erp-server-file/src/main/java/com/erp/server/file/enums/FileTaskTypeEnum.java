package com.erp.server.file.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileTaskTypeEnum implements EnumMessage {
    ASYNC_IMPORT("asyncImport","异步导入"),
    ASYNC_EXPORT("asyncExport","异步导出"),
    MERGE_LABEL("mergeLabel","合并标签");

    private final String code;
    private final String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
