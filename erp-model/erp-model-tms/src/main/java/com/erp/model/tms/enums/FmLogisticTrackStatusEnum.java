package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 头程物流运输状态
 * @author Lambda
 * @Classname LogisticTrackStatusEnum
 * @Date 2023-11-15 17:38
 * @Created by yl
 */
public enum FmLogisticTrackStatusEnum implements EnumMessage {
    WAIT_ORDER("waitOrder","待下单"),
    ORDERED("ordered","已下单"),
    PICKUP("pickup","已揽收"),
    INSPECTING("Inspecting","查验中"),
    TRACK_ING("trackIng","运输中"),
    ARRIVED("arrived","已到港"),
    SIGN("sign","已签收"),
    EXCEPTION("exception","运输异常"),
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


    FmLogisticTrackStatusEnum(String code, String name){
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }
    @Override
    public String getName() {
        return this.name;
    }


    public static List<String> getStatusNotWaitOrder(){
        return Arrays.stream(FmLogisticTrackStatusEnum.values()).filter(e -> !e.equals(WAIT_ORDER)).map(FmLogisticTrackStatusEnum::getCode).collect(Collectors.toList());
    }

    /**
     * 通过code查询
     * LogisticsMethodEnum
     * 枚举
     */
    public static FmLogisticTrackStatusEnum getByName(String name) {
        return Stream.of(FmLogisticTrackStatusEnum.values())
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * 通过code查询
     * 枚举
     */
    public static FmLogisticTrackStatusEnum getNameByCode(String code) {
        return Stream.of(FmLogisticTrackStatusEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
