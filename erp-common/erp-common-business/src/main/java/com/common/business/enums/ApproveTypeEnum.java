package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @date 2023/3/20 9:30
 */
public enum ApproveTypeEnum {

    PASS("pass", "审核通过"),
    REJECT("reject", "审核不通过"),

//    REJECT_PREVIOUS("reject_previous", "驳回上个节点"),
    /**
     * 驳回指定节点
     */
    REJECT_APPOINT("reject_appoint", "驳回指定节点"),
    REVOKE("revoke", "撤回流程"),
    CANCEL("cancel", "撤销"),
    ;
    @JsonValue
    @EnumValue
    private String status;
    private String name;

    ApproveTypeEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }

    public String getStatus() {
        return status;
    }


    public String getName() {
        return name;
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (ApproveTypeEnum item : ApproveTypeEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static ApproveTypeEnum getByCode(String code) {
        return Arrays.stream(ApproveTypeEnum.values())
                .filter(e -> e.getStatus().equals(code))
                .findFirst()
                .orElse(null);
    }

}
