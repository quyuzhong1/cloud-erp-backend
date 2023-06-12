package com.erp.model.scm.enums;

import com.common.business.enums.ApproveStatusEnum;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/27 10:52
 */
public enum PageListTypeEnum {

    TO_BE_APPROVE("toBeApprove", "待审批", Lists.newArrayList(ApproveStatusEnum.APPROVE_ING)),
    APPROVE("approve", "审核通过", Lists.newArrayList(ApproveStatusEnum.APPROVE)),
    REJECT("reject", "不通过", Lists.newArrayList(ApproveStatusEnum.REJECT));


    private String code;
    private String name;

    // 业务单据状态
    private List<ApproveStatusEnum> approveStatusList;

    PageListTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    PageListTypeEnum(String code, String name, List<ApproveStatusEnum> approveStatusList) {
        this.code = code;
        this.name = name;
        this.approveStatusList = approveStatusList;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public List<ApproveStatusEnum> getApproveStatusList() {
        return approveStatusList;
    }
}
