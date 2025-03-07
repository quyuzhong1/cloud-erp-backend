package com.erp.model.oms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 上传记录 上传状态 枚举
 * </p>
 *
 * @author zdy
 * @since 2025-03-07 14:15:46
 */
public enum InvoiceInfoUploadStatusEnum implements EnumMessage {
	WAITUPLOAD("waitUpload", "待上传"),
	UPLOADFAILED("uploadFailed", "上传失败"),
	UPLOADSUCCESS("uploadSuccess", "上传成功"),
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

    InvoiceInfoUploadStatusEnum(String code, String name) {
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
        for (InvoiceInfoUploadStatusEnum statusEnum : InvoiceInfoUploadStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
