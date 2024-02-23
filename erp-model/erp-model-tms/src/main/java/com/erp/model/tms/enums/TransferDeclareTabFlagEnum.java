package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.StringUtils;

/**
 * 中转报关tab页状态
 */
public enum  TransferDeclareTabFlagEnum implements EnumMessage {
    WAIT_UPLOAD("waitUpload", "待上传"),
    UPLOAD_FAILURE("uploadFailure", "上传失败"),
    LOGISTICS_UN_OUTSTOCK("logisticsUnOutstock", "物流商未出库"),
    LOGISTICS_OUTSTOCK("logisticsOutstock", "物流商已出库"),
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

    TransferDeclareTabFlagEnum(String code, String name) {
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
        for (TransferDeclareTabFlagEnum statusEnum : TransferDeclareTabFlagEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
