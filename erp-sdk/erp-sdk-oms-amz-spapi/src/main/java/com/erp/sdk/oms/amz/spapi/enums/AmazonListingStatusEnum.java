package com.erp.sdk.oms.amz.spapi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 亚马逊SP-API Listing状态
 *
 * @author Jim
 * @date 2023/11/1
 */
@Getter
@AllArgsConstructor
public enum AmazonListingStatusEnum {

    ACTIVE("Active",	"在售"),
    INACTIVE("Inactive",	"停售"),
    INCOMPLETE("Incomplete",	"未完成"),
    UN_KNOW("unKnow",	"未知状态"),


    ;


    /**
     * 商城编号
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 名称
     */
    private final String name;

}
