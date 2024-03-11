package com.erp.sdk.oms.amz.spapi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 亚马逊SP-API 配置物流记录处理状态
 *
 * @author Jim
 * @date 2024/03/11
 */
@Getter
@AllArgsConstructor
public enum AmazonHandleStatusEnum {

    NONE("-1",	"无需处理(已有订单直接处理)"),
    WAIT_DOWNLOAD("0",	"待下载订单(检查订单下载处理)"),
    WAIT_HANDLE("1",	"待处理销售出库单(订单已下载处理)"),
    HANDLE("2",	"已处理"),

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
