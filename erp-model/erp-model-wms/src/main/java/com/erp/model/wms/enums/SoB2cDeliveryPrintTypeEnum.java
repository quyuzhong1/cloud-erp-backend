package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

public enum SoB2cDeliveryPrintTypeEnum implements EnumMessage {
    ALL("all", "全部"),
    LOGISTICS_BILL("logisticsBill", "物流面单"),
    ALLOCATE_CARGO_BILL("allocateCargoBill", "配货单");

    @EnumValue
    @JsonValue
    private String status;
    private String name;

    SoB2cDeliveryPrintTypeEnum(String status, String name) {
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
            for (SoB2cDeliveryPrintTypeEnum item : SoB2cDeliveryPrintTypeEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static SoB2cDeliveryPrintTypeEnum getByCode(String code) {
        return Arrays.stream(SoB2cDeliveryPrintTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }
}
