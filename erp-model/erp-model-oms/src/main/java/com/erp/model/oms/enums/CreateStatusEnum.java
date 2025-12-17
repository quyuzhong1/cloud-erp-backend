package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zdy
 * @version 1.0
 * @description: 创建状态枚举
 * @date 2025/8/22 12:27
 */
public enum CreateStatusEnum implements EnumMessage {
    //创建状态(wait待创建,creating创建中,success创建成功,failed创建失败)
    WAIT("wait",  "待创建"),
    CREATING("creating",  "创建中"),
    SUCCESS("success",  "创建成功"),
    FAILED("failed",  "创建失败"),
    CANCEL("cancel",  "取消创建"),
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


    CreateStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CreateStatusEnum soB2cCategoryTypeEnum : CreateStatusEnum.values()) {
            if (code.equals(soB2cCategoryTypeEnum.getCode())) {
                return soB2cCategoryTypeEnum.getName();
            }
        }
        return "";
    }
}
