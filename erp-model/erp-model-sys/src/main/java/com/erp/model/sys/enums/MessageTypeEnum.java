package com.erp.model.sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum MessageTypeEnum {
    QC("qc","质检通知", "请及时查收消息"),
    PDA("pda","PDA升级通知", "版本升级通知"),
    SYS("sys","系统公告", "版本升级通知"),
    SYS_VERSION("sysVersion","版本更新", "版本更新内容"),
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
     * 描述
     */
    private String remark;

    MessageTypeEnum(String code, String name, String remark) {
        this.code = code;
        this.name = name;
        this.remark = remark;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getRemark() {
        return remark;
    }

    public static String getName(String code) {
        for (MessageTypeEnum messageTypeEnum : MessageTypeEnum.values()) {
            if (code.equals(messageTypeEnum.getCode())) {
                return messageTypeEnum.getName();
            }
        }
        return "";
    }
    
    public static MessageTypeEnum getMessageTypeEnum(String code) {
    	for (MessageTypeEnum messageTypeEnum : MessageTypeEnum.values()) {
    		if (code.equals(messageTypeEnum.getCode())) {
    			return messageTypeEnum;
    		}
    	}
    	return null;
    }

    public static String getCodeByName(String name) {
        for (MessageTypeEnum messageTypeEnum : MessageTypeEnum.values()) {
            if(Objects.equals(name, messageTypeEnum.name)) {
                return messageTypeEnum.code;
            }
        }
        return "";
    }

}
