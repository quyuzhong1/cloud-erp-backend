package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum OrderProcessingLableEnum implements EnumMessage {
    OUTSTOCK("outstock", "出库"),
    FROZEN("frozen", "发货冻结"),
    UN_SHIPPED("unShipped", "七日未发"),
    ;

    @EnumValue
    @JsonValue
    private String status;
    private String name;

    OrderProcessingLableEnum(String status, String name) {
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
            for (OrderProcessingLableEnum item : OrderProcessingLableEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static OrderProcessingLableEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equals(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(OrderProcessingLableEnum.values()).map(OrderProcessingLableEnum::getStatus).collect(Collectors.toList());
    }
}
