package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 组包上传状态
 */
public enum PackageUploadStatusEnum implements EnumMessage {

    NOT("not", "无需上传"),
    WAIT("wait", "待上传"),
    FAILURE("failure", "上传失败"),
    SUCCESS("success", "上传成功"),
    CANCEL("cancel", "上传成功"),
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

    PackageUploadStatusEnum(String code, String name) {
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
        for (PackageUploadStatusEnum statusEnum : PackageUploadStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
