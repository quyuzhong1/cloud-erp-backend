package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/15 17:28
 */
public enum ApproveStatusEnum implements EnumMessage  {
    WAIT_SUBMIT("waitSubmit", "待提交", "待提交"),
    APPROVE_ING("approveIng", "审核中","待审核"),
    REJECT("reject", "审核不通过","不通过"),
    APPROVE("approve", "已审核","已审核");

    @EnumValue
    @JsonValue
    private String status;
    private String name;
    private String tableName;

    ApproveStatusEnum(String status, String name,String tableName) {
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
            for (ApproveStatusEnum item : ApproveStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
    public static String getTableName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (ApproveStatusEnum item : ApproveStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getTableName();
                }
            }
        }
        return "";
    }

    public static ApproveStatusEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equalsIgnoreCase(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(ApproveStatusEnum.values()).map(ApproveStatusEnum::getStatus).collect(Collectors.toList());
    }
    public static Boolean allowUpdateStatus(ApproveStatusEnum approveStatus) {
        return approveStatus.equals(ApproveStatusEnum.WAIT_SUBMIT) || approveStatus.equals(ApproveStatusEnum.REJECT);
    }

    public static ApproveStatusEnum transferApproveType(ApproveTypeEnum approveType) {
        return Objects.equals(ApproveTypeEnum.PASS, approveType) ? ApproveStatusEnum.APPROVE : ApproveStatusEnum.REJECT;
    }
    public static ApproveStatusEnum transferApproveType(String approveType) {
        return Objects.equals(ApproveTypeEnum.PASS.getStatus(), approveType) ? ApproveStatusEnum.APPROVE : ApproveStatusEnum.REJECT;
    }
}
