package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物流支付类型
 * @author will
 * @date 2026/1/20 10:45
 */
public enum logisticsPayTypeEnum implements EnumMessage  {
    PAY("pay", "付款"),
    REFUND("refund", "退款");
    @EnumValue
    @JsonValue
    private String status;
    private String name;

    logisticsPayTypeEnum(String status, String name) {
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
            for (logisticsPayTypeEnum item : logisticsPayTypeEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static logisticsPayTypeEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equals(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(logisticsPayTypeEnum.values()).map(logisticsPayTypeEnum::getStatus).collect(Collectors.toList());
    }

}
