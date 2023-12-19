package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PickingTypeEnum implements EnumMessage {
    SINGLE_ITEM_SINGLE("singleItemSingle", "单品单数"),
    SINGLE_ITEM_MULTI("singleItemMulti", "单品多数"),
    MULTI_ITEM_MULTI("multiItemMulti", "多品多数"),
    ;

    @EnumValue
    @JsonValue
    private String status;
    private String name;

    PickingTypeEnum(String status, String name) {
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
            for (PickingTypeEnum item : PickingTypeEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static PickingTypeEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equals(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(PickingTypeEnum.values()).map(PickingTypeEnum::getStatus).collect(Collectors.toList());
    }
}
