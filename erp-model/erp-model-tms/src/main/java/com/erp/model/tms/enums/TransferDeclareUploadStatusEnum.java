package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 中转报关上传状态
 */
public enum TransferDeclareUploadStatusEnum implements EnumMessage {

    WAIT_UPLOAD("waitUpload", "待上传"),
    UPLOAD_FAILURE("uploadFailure", "上传失败"),
    UPLOAD_SUCCESS("uploadSuccess", "上传成功"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    TransferDeclareUploadStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (TransferDeclareUploadStatusEnum statusEnum : TransferDeclareUploadStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
