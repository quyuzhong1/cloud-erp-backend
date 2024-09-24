package com.erp.model.tms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 费用分摊状态
 * @date 2024-08-22
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum CostAllocationStatusEnum implements EnumMessage {
    ALREADY("already", "已分摊"),
    PART("part", "部分分摊"),
    NOT("not", "未分摊")
    ;

    private String code;
    private String name;

    public static String getName(String code){
        for (CostAllocationStatusEnum statusEnum : values()) {
            if(statusEnum.getCode().equals(code)){
                return statusEnum.getName();
            }
        }
        return "";
    }
}
