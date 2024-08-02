package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AbnormalCauseEnum implements EnumMessage {

    GENERATION_WAVE("generation_wave", "生成波次缺货自动触发补货中"),
    PICK_MARKINGS("pick_markings", "拣货时手动标记缺货"),
    EQUIPMENT_SORTING("equipment_sorting", "设备异常口订单-（尺寸超限/重量超限/单号不存在/未配置拣货口）")
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
}
