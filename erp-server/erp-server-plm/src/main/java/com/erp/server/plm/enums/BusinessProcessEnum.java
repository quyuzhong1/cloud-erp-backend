package com.erp.server.plm.enums;

/**
 * @Classname BusinessProcessEnum
 * @Description TODO
 * @Date 2022-10-18 17:20
 * @Created by yl
 */
public enum BusinessProcessEnum {

    REVIEW_TASK("reviewTask", "评审任务"),
    DOCS_CHANGE("changeDocs", "文档变更");


    private String businessType;

    private String businessName;

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessType() {
        return businessType;
    }

    public String getBusinessName() {
        return businessName;
    }

    BusinessProcessEnum(String businessType, String businessName) {
        this.businessType = businessType;
        this.businessName = businessName;
    }
}
