package com.erp.server.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;

/**
 * @author jack
 * @date 2025-04-06
 */
public enum AfterSaleStatusEnum implements EnumMessage {
    REPAIR_REQUEST("repairRequest","客户报修","客户报修"),
    APPROVE_ING("approveIng","审核中","客服审核"),
    TO_BE_RETURNED("toBeReturned","待寄回","客户寄件"),
    AFTER_SALES_RECEIVED("afterSalesReceived","售后签收","售后签收"),
    INSPECTION("inspection","检测","售后检测"),
    REPAIR("repair","维修中","维修作业"),
    TO_BE_SHIPPED("toBeShipped","待寄出","售后发货"),
//    FINISHED("finished","已完成","已完成"),
//    TERMINATED("terminated","已终止","已终止")
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;
    private String node;

    AfterSaleStatusEnum(String code, String name,String node) {
        this.code = code;
        this.name = name;
        this.node = node;
    }


    @Override
    public String getCode() {
        return code;
    }
    @Override
    public String getName() {
        return name;
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (ApproveStatusEnum item : ApproveStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static AfterSaleStatusEnum getByCode(String code){
        return Arrays.stream(values()).filter(a -> a.getCode().equalsIgnoreCase(code))
                .findFirst().orElse(null);
    }



}
