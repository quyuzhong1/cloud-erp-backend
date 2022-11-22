package com.erp.server.plm.enums;

/**
 * @Classname BusinessProcessEnum
 * @Description TODO
 * @Date 2022-10-18 17:20
 * @Created by yl
 */
public enum BusinessProcessEnum {

    REVIEW_TASK("reviewTask", "评审任务"),
    DOCS_CHANGE("changeDocs", "文档变更"),
    GENERAL_TASK("generalTask", "一般任务审核流程"),
    CONCEPT_DESIGN("conceptDesign", "产品概念设计申请流程"),
    ID_CONFIRM("idConfirm", "ID确认书流程"),
    PROJECT_APPROVAL("projectApproval", "立项会议流程");


    private String businessKey;

    private String businessName;

    public void setBusinessType(String businessType) {
        this.businessKey = businessType;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public String getBusinessName() {
        return businessName;
    }

    BusinessProcessEnum(String businessType, String businessName) {
        this.businessKey = businessType;
        this.businessName = businessName;
    }
}
