package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 波次详情-拣货状态
 * @date 2024-07-02
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum PickingStatusEnum implements EnumMessage {
    PICK_ING("pickIng", "拣货中"),
    FINISH("finish", "已完成"),
    NOT_START("notStart", "未开始")
    ;

    private String code;
    private String name;

    public static String getName(String code){
        for (PickingStatusEnum statusEnum : PickingStatusEnum.values()) {
            if(statusEnum.getCode().equals(code)){
                return statusEnum.getName();
            }
        }
        return "";
    }
}
