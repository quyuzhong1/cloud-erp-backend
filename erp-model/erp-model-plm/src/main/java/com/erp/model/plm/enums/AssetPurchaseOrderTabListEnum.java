package com.erp.model.plm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: wtr
 * @Date: 2025/10/29 8:52
 * @Param:
 * @Return:
 * @Description:
 **/
public enum AssetPurchaseOrderTabListEnum implements EnumMessage {

    ALL("all", "全部","全部"),
    WAIT_SUBMIT("waitSubmit", "待提交", "待提交"),
    APPROVE_ING("approveIng", "审核中","审核中"),
    WAIT_RECEIVE("waitReceive", "待验收","待验收"),
    ALL_RECEIVE("allReceive", "已验收","已验收"),
    CLOSE("close", "已关闭","已关闭"),
    REJECT("reject", "审核不通过","不通过");

    @EnumValue
    @JsonValue
    private String status;
    private String name;
    private String tableName;

    AssetPurchaseOrderTabListEnum(String status, String name,String tableName) {
        this.status = status;
        this.name = name;
        this.tableName = tableName;
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

    public String getTableName() {
        return tableName;
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (AssetPurchaseOrderTabListEnum item : AssetPurchaseOrderTabListEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
    public static String getTableName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (AssetPurchaseOrderTabListEnum item : AssetPurchaseOrderTabListEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getTableName();
                }
            }
        }
        return "";
    }

    public static AssetPurchaseOrderTabListEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equalsIgnoreCase(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(AssetPurchaseOrderTabListEnum.values()).map(AssetPurchaseOrderTabListEnum::getStatus).collect(Collectors.toList());
    }
}
