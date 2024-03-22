package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 头程物流运输状态
 * @author Lambda
 * @Classname LogisticTrackStatusEnum
 * @Description TODO
 * @Date 2023-11-15 17:38
 * @Created by yl
 */
public enum FmLogisticTrackStatusEnum implements EnumMessage {
    WAIT_ORDER("waitOrder","待下单","waitOrder","待下单"),
    ORDERED("ordered","已下单","ordered","已下单"),
    TRACK_ING("trackIng","运输途中","trackIng","运输途中"),
    ARRIVED("arrived","已到港","arrived","已到港"),
    SIGN("sign","已签收","sign","已签收"),
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

    /**
     * 组别
     */
    private String group;

    /**
     * 组别
     */
    private String groupName;


    FmLogisticTrackStatusEnum(String code, String name, String group, String groupName){
        this.code = code;
        this.name = name;
        this.group = group;
        this.groupName = groupName;
    }

    @Override
    public String getCode() {
        return this.code;
    }
    @Override
    public String getName() {
        return this.name;
    }


    public String getGroup() {
        return this.group;
    }

    public String getGroupName() {
        return this.groupName;
    }
    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (FmLogisticTrackStatusEnum item : FmLogisticTrackStatusEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }

    public static String getGroupName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (FmLogisticTrackStatusEnum item : FmLogisticTrackStatusEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }


}
