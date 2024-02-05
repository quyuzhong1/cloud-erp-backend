package com.erp.model.tms.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;

/**
 * 产品备案状态枚举
 */
@Getter
public enum ProductRegistrationStatusEnum implements EnumMessage {

    DRAFT("draft","草稿"),
    REGISTERING("registering","备案中"),
    REGISTERED("registered","已备案"),
    FREEZE("freeze","冻结"),
    ;

    /**
     * 类型
     */
    private String code;
    /**
     * 名称
     */
    private String name;

    ProductRegistrationStatusEnum(String code, String name){
        this.code = code;
        this.name = name;
    }

}
