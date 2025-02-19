package com.erp.model.dmp.enums;

public enum OutputTaskRecordMergeStatusEnum {
    WAIT_MERGE("waitMerge", "待合并"),
    MERGE("merge", "已合并"),
    ;
    private String code;

    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    OutputTaskRecordMergeStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
