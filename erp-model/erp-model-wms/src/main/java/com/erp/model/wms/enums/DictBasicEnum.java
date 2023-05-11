package com.erp.model.wms.enums;

/**
 * @author Lambda
 * @Classname DictBasicEnum
 * @Description TODO
 * @Date 2023-03-20 14:15
 * @Created by yl
 */
public enum DictBasicEnum {

    WAREHOUSE_TYPE("warehouseType", "", "仓库类型"),
    QC_REPORT_RESULT("qcReportResult", "", "质检单质检结果"),
    HANDLE_MODE_TYPE("handleModeType", "", "质检单处理措施"),
    TRANSFER_TYPE("transferType", "", "调拨类型"),
    TRANSFER_DIRECTION("transferDirection", "", "调拨方向"),

    ;


    private String key;
    private String remark;
    private String desc;


    DictBasicEnum(String key, String remark, String desc) {
        this.key = key;
        this.remark = remark;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getRemark() {
        return remark;
    }

    public String getDesc() {
        return desc;
    }
}
