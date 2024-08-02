package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PickingWaveTypeEnum implements EnumMessage {

    MIXED_WAVE("mixedWave", "混合波次"),
    SAME_WAVE("sameWave","同类波次")
    ;

    private final String code;
    private final String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code){
        if(code.equals(PickingWaveTypeEnum.MIXED_WAVE.getCode())){
            return PickingWaveTypeEnum.MIXED_WAVE.getName();
        }
        if(code.equals(PickingWaveTypeEnum.SAME_WAVE.getCode())){
            return PickingWaveTypeEnum.SAME_WAVE.getName();
        }
        return "";
    }
}
