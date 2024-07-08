package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 拣货波次状态
 * @date 2024-06-27
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum WaveStatusEnum implements EnumMessage {
    AWAIT_PICK("awaitPick", "待拣货"),
    PICK_ING("pickIng", "拣货中"),
    HANG_UP("hangUp", "挂起"),
    FINISH("finish", "已完成"),
    ;

    private String code;
    private String name;

    public static String getNameByCode(String code){
        for (WaveStatusEnum statusEnum : WaveStatusEnum.values()) {
            if(statusEnum.getCode().equals(code)){
                return statusEnum.getName();
            }
        }
        return "";
    }
}
