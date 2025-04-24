package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 采购退货单确认状态
 */
public enum PoReturnConfirmStatusEnum implements EnumMessage {
    WAIT_CONFIRM("waitConfirm", "待确认"),
    CONFIRM("confirm", "已确认"),
    ;

    @EnumValue
    @JsonValue
    private String status;
    private String name;

    PoReturnConfirmStatusEnum(String status, String name) {
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
            for (PoReturnConfirmStatusEnum item : PoReturnConfirmStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static List<String> getStatusList() {
        return Arrays.stream(PoReturnConfirmStatusEnum.values()).map(PoReturnConfirmStatusEnum::getStatus).collect(Collectors.toList());
    }
}
