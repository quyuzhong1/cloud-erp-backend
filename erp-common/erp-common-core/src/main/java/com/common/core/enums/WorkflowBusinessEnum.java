package com.common.core.enums;

/**
 * @Classname BusinessProcessEnum
 * @Description TODO
 * @Date 2022-10-18 17:20
 * @Created by yl
 */
public enum WorkflowBusinessEnum {

    REVIEW_TASK("reviewTask", "评审任务","plm"),
    DOCS_CHANGE("changeDocs", "文档变更","plm"),
    GENERAL_TASK("generalTask", "一般任务审核流程","plm"),
    CONCEPT_DESIGN("conceptDesign", "产品概念设计申请流程","plm"),
    ID_CONFIRM("idConfirm", "ID确认书流程","plm"),
    PROJECT_APPROVAL("projectApproval", "立项会议流程","plm"),
    PRODUCT_DETAIL("productDetail", "SKU审核","plm"),
    BOM_AUDIT("bom","bom审核","plm"),
    BOM_CHANGE("bomChange","bom变更审核","plm"),
    SKU_CHANGE("skuChange","sku变更审核","plm"),
    SCHEDULE_TASK("taskSchedule","任务排期审核","plm");


    private String businessType;

    private String businessName;

    private String platform;

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }


    public String getBusinessType() {
        return businessType;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getPlatform() {
        return platform;
    }

    WorkflowBusinessEnum(String businessType, String businessName,String platform) {
        this.businessType = businessType;
        this.businessName = businessName;
        this.platform = platform;
    }
}
