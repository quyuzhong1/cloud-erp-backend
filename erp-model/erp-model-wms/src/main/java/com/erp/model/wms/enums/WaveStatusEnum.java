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
    AWAIT_PICK("await_pick", "待拣货"),
    PICK_ING("pick_ing", "拣货中"),
    HANG_UP("hang_up", "挂起"),
    FINISH("finish", "已完成"),
    ;

    private String code;
    private String name;

    public static String getNameByCode(String code){
        if(WaveStatusEnum.AWAIT_PICK.getCode().equals(code)){
            return WaveStatusEnum.AWAIT_PICK.getName();
        }
        if(WaveStatusEnum.PICK_ING.getCode().equals(code)){
            return WaveStatusEnum.PICK_ING.getName();
        }
        if(WaveStatusEnum.HANG_UP.getCode().equals(code)){
            return WaveStatusEnum.HANG_UP.getName();
        }
        if(WaveStatusEnum.FINISH.getCode().equals(code)){
            return WaveStatusEnum.FINISH.getName();
        }
        return "";
    }
}
