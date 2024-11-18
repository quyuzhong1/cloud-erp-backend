package com.common.business.enums;

import lombok.Getter;

/**
 * @Classname BusinessProcessEnum

 * @Date 2022-10-18 17:20
 * @Created by yl
 */
@Getter
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
    SKU_CHANGE("changeSku","sku变更审核","plm"),
    SCHEDULE_TASK("taskSchedule","任务排期审核","plm");


    private final String businessType;

    private final String businessName;

    private final String platform;


    WorkflowBusinessEnum(String businessType, String businessName,String platform) {
        this.businessType = businessType;
        this.businessName = businessName;
        this.platform = platform;
    }
}
