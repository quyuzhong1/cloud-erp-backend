package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流运输状态
 * @author Lambda
 * @Classname LogisticTrackStatusEnum
 * @Date 2023-11-15 17:38
 * @Created by yl
 */
public enum LogisticTrackStatusEnum implements EnumMessage {
    /**
     * 小包
     */
    WAIT_ORDER("waitOrder","待下单","waitOrder","待下单"),
    ORDERED("ordered","已下单","ordered","已下单"),
    NOT_FIND("notFind","查询不到","notFind","查询不到"),
    WAIT_COLLECT("waitCollect","等待揽收", Constants.TRACK_ING1,"运输途中"),
    TRACK_ING(Constants.TRACK_ING1,"运输途中", Constants.TRACK_ING1,"运输途中"),
    ARRIVE_WAIT_TAKE("arriveWaitTake","到达待取", Constants.TRACK_ING1,"运输途中"),
    DELIVERY_ING("deliveryIng","派送途中", Constants.TRACK_ING1,"运输途中"),
    DELIVERY_FAIL("deliveryFail","投递失败", Constants.TRACK_ING1,"运输途中"),
    SIGN("sign","成功签收", Constants.RECEIVED,"已签收"),
    MANUAL_COMPLETE("manualComplete","手动完结", Constants.RECEIVED,"已签收"),
    SYSTEM_COMPLETE("systemComplete","系统完结", Constants.RECEIVED,"已签收"),
    MAYBE_EXCEPTION("maybeException","可能异常","trackException","运输异常"),
    TRANSPORT_LONG("transportLong","运输过久","trackException","运输异常"),
    /**
     * 海运
     */
    OCEAN_TRACK_ING("oceanTrackIng","运输中", Constants.TRACK_ING1,"运输中"),
    OCEAN_ARRIVE("oceanArrive","已到港","arrive","已到港"),
    OCEAN_HOLD("oceanHold","查验中","hold","查验中")
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


    LogisticTrackStatusEnum(String code, String name,String group,String groupName){
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
        for (LogisticTrackStatusEnum item : LogisticTrackStatusEnum.values()) {
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
        for (LogisticTrackStatusEnum item : LogisticTrackStatusEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }


    private static class Constants {
        public static final String TRACK_ING1 = "trackIng";
        public static final String RECEIVED = "received";
    }
}
