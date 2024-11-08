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
 * @author zdy
 * @version 1.0

 * @date 2024/8/15 17:28
 */
public enum TabApproveStatusEnum implements EnumMessage  {
    WAIT_SUBMIT("waitSubmit", "待提交"),
    APPROVE_ING("approveIng", "待审核"),
    REJECT("reject", "不通过"),
    APPROVE("approve", "已审核");

    @EnumValue
    @JsonValue
    private String status;
    private String name;

    TabApproveStatusEnum(String status, String name) {
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
            for (TabApproveStatusEnum item : TabApproveStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static TabApproveStatusEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equalsIgnoreCase(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(TabApproveStatusEnum.values()).map(TabApproveStatusEnum::getStatus).collect(Collectors.toList());
    }
    public static Boolean allowUpdateStatus(TabApproveStatusEnum approveStatus) {
        return approveStatus.equals(TabApproveStatusEnum.WAIT_SUBMIT) || approveStatus.equals(TabApproveStatusEnum.REJECT);
    }

    public static TabApproveStatusEnum transferApproveType(ApproveTypeEnum approveType) {
        return Objects.equals(ApproveTypeEnum.PASS, approveType) ? TabApproveStatusEnum.APPROVE : TabApproveStatusEnum.REJECT;
    }
    public static TabApproveStatusEnum transferApproveType(String approveType) {
        return Objects.equals(ApproveTypeEnum.PASS.getStatus(), approveType) ? TabApproveStatusEnum.APPROVE : TabApproveStatusEnum.REJECT;
    }
}
