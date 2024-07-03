package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 装箱任务-单据类型
 */
public enum PickingSourceTypeEnum implements EnumMessage {
    B2B("B2B", "B2B"),
    FBA("FBA", "FBA"),
    THIRD("third", "第三方仓"),
    ;

    @EnumValue
    @JsonValue
    private String status;
    private String name;

    PickingSourceTypeEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }


    public String getStatus() {
        return status;
    }
    @Override
    public String getCode() {
        return status;
    }
    @Override
    public String getName() {
        return name;
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (PickingSourceTypeEnum item : PickingSourceTypeEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static PickingSourceTypeEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equals(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(PickingSourceTypeEnum.values()).map(PickingSourceTypeEnum::getStatus).collect(Collectors.toList());
    }
}
