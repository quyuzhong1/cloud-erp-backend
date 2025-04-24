package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 要货申请单据状态
 * @Author Luo_WG
 * @Date 2023/11/20 16:40
 **/
public enum RequisitionApplicationStatusEnum implements EnumMessage  {
    WAIT_SUBMIT("waitSubmit", "待提交"),
    WAIT_HANDLE("waitHandle", "待处理"),
    HANDLE_ING("handleIng", "处理中"),
    HANDLE("handle", "已处理");

    @EnumValue
    @JsonValue
    private String status;
    private String name;

    RequisitionApplicationStatusEnum(String status, String name) {
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
            for (RequisitionApplicationStatusEnum item : RequisitionApplicationStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static RequisitionApplicationStatusEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getStatus().equals(status))
                .findFirst().orElse(null);
    }

    public static List<String> getStatusList() {
        return Arrays.stream(RequisitionApplicationStatusEnum.values()).map(RequisitionApplicationStatusEnum::getStatus).collect(Collectors.toList());
    }

    public static List<String> getPickingList(){
        return Arrays.asList(HANDLE_ING.getCode());
    }
}
