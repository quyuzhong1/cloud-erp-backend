package com.erp.model.scm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.enums.ApproveTypeEnum;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: wtr
 * @Date: 2025/10/21 15:36
 * @Param:
 * @Return:
 * @Description:
 **/
public enum AssetApproveStatusEnum implements EnumMessage {

    WAIT_SUBMIT("waitSubmit", "待提交", "待提交"),
    APPROVE_ING("approveIng", "审核中","待审核"),
    APPROVE("approve", "已审核","已审核"),
    REJECT("reject", "审核不通过","不通过"),
    ALL("all", "全部","全部");

    @EnumValue
    @JsonValue
    private String status;
    private String name;
    private String tableName;

    AssetApproveStatusEnum(String status, String name,String tableName) {
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
            for (com.common.business.enums.ApproveStatusEnum item : com.common.business.enums.ApproveStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
    public static String getTableName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (com.common.business.enums.ApproveStatusEnum item : com.common.business.enums.ApproveStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getTableName();
                }
            }
        }
        return "";
    }

    public static AssetApproveStatusEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equalsIgnoreCase(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(com.common.business.enums.ApproveStatusEnum.values()).map(com.common.business.enums.ApproveStatusEnum::getStatus).collect(Collectors.toList());
    }
    public static Boolean allowUpdateStatus(com.common.business.enums.ApproveStatusEnum approveStatus) {
        return approveStatus.equals(com.common.business.enums.ApproveStatusEnum.WAIT_SUBMIT) || approveStatus.equals(com.common.business.enums.ApproveStatusEnum.REJECT);
    }
    public static Boolean allowUpdateStatus(String approveType) {
        return approveType.equals(com.common.business.enums.ApproveStatusEnum.WAIT_SUBMIT.getCode()) || approveType.equals(com.common.business.enums.ApproveStatusEnum.REJECT.getCode());
    }
    public static com.common.business.enums.ApproveStatusEnum transferApproveType(ApproveTypeEnum approveType) {
        if (Objects.equals(ApproveTypeEnum.PASS, approveType)) {
            return com.common.business.enums.ApproveStatusEnum.APPROVE;
        } else if (Objects.equals(ApproveTypeEnum.CANCEL, approveType)) {
            return  com.common.business.enums.ApproveStatusEnum.WAIT_SUBMIT;
        } else {
            return  com.common.business.enums.ApproveStatusEnum.REJECT;
        }
    }
    public static com.common.business.enums.ApproveStatusEnum transferApproveType(String approveType) {
        if (Objects.equals(ApproveTypeEnum.PASS.getStatus(), approveType)) {
            return com.common.business.enums.ApproveStatusEnum.APPROVE;
        } else if (Objects.equals(ApproveTypeEnum.CANCEL.getStatus(), approveType)) {
            return  com.common.business.enums.ApproveStatusEnum.WAIT_SUBMIT;
        } else {
            return  com.common.business.enums.ApproveStatusEnum.REJECT;
        }
    }
}
