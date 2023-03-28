package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 17:28
 */
public enum ApproveStatusEnum {

    WAIT_SUBMIT("waitSubmit", "待提交"),
    APPROVE_ING("approveIng", "审核中"),
    REJECT("reject", "审核不通过"),
    APPROVE("approve", "已审核");

    @EnumValue
    private String status;
    private String name;

    ApproveStatusEnum(String status, String name) {
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
            for (ApproveStatusEnum item : ApproveStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }


    public static ApproveStatusEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equals(status))
                .findFirst().orElse(null);
    }
}
