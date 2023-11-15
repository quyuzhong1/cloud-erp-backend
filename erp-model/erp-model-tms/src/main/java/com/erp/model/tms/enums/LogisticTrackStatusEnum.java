package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流运输状态
 * @author Lambda
 * @Classname LogisticTrackStatusEnum
 * @Description TODO
 * @Date 2023-11-15 17:38
 * @Created by yl
 */
public enum LogisticTrackStatusEnum implements EnumMessage {
    ALL("all","全部"),
    RECEIVED("received","已签收"),
    TRACK_ING("trackIng","运输途中"),
    TRACK_EXCEPTION("trackException","运输异常"),
    NOT_FIND("notFind","查询不到"),
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

    LogisticTrackStatusEnum(String code, String name){
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

}
